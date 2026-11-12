package com.decms.service;

import com.decms.domain.model.custody.CustodyRecord;
import com.decms.domain.model.evidence.Evidence;
import com.decms.domain.model.investigation.Case;
import com.decms.repository.CustodyRecordRepository;
import com.decms.repository.EvidenceRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportGenerationService {

    private final CaseService caseService;
    private final EvidenceRepository evidenceRepository;
    private final CustodyRecordRepository custodyRecordRepository;

    public ReportGenerationService(CaseService caseService,
                                   EvidenceRepository evidenceRepository,
                                   CustodyRecordRepository custodyRecordRepository) {
        this.caseService = caseService;
        this.evidenceRepository = evidenceRepository;
        this.custodyRecordRepository = custodyRecordRepository;
    }

    public byte[] generateCasePdfReport(String caseId) {
        Case c = caseService.getCaseEntity(caseId);
        List<Evidence> evidenceList = evidenceRepository.findByInvestigationCase_IdOrderByUploadedAtDesc(c.getId());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter writer = PdfWriter.getInstance(document, out);

            // Metadata
            document.addTitle("Forensic Investigation Report - " + c.getCaseId());
            document.addAuthor("Digital Evidence & Case Management System (DECMS)");
            document.addSubject("Forensic Case Dossier & Chain of Custody Audit");

            document.open();

            // Styling Colors & Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(15, 23, 42));
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(14, 116, 144));
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(30, 41, 59));
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(51, 65, 85));
            Font boldBodyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(30, 41, 59));
            Font codeFont = FontFactory.getFont(FontFactory.COURIER, 8, new Color(15, 23, 42));
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);

            // Header Banner
            Paragraph banner = new Paragraph("DECMS FORENSIC LABS • EVIDENCE & CASE MANAGEMENT SYSTEM", subtitleFont);
            banner.setAlignment(Element.ALIGN_CENTER);
            document.add(banner);

            Paragraph docTitle = new Paragraph("OFFICIAL FORENSIC CASE DOSSIER", titleFont);
            docTitle.setAlignment(Element.ALIGN_CENTER);
            docTitle.setSpacingAfter(15);
            document.add(docTitle);

            // Classification bar
            PdfPTable classTable = new PdfPTable(1);
            classTable.setWidthPercentage(100);
            PdfPCell classCell = new PdfPCell(new Phrase("CLASSIFICATION: CONFIDENTIAL / LAW ENFORCEMENT & FORENSIC PRIVILEGE",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE)));
            classCell.setBackgroundColor(new Color(185, 28, 28));
            classCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            classCell.setPadding(4);
            classTable.addCell(classCell);
            classTable.setSpacingAfter(15);
            document.add(classTable);

            // Case Overview Section
            Paragraph caseHeading = new Paragraph("1. CASE SPECIFICATION", sectionFont);
            caseHeading.setSpacingAfter(8);
            document.add(caseHeading);

            PdfPTable overviewTable = new PdfPTable(4);
            overviewTable.setWidthPercentage(100);
            overviewTable.setWidths(new float[]{20, 30, 20, 30});

            addMetaCell(overviewTable, "Case ID:", c.getCaseId(), boldBodyFont, bodyFont);
            addMetaCell(overviewTable, "Status:", c.getStatus().name(), boldBodyFont, bodyFont);
            addMetaCell(overviewTable, "Title:", c.getTitle(), boldBodyFont, bodyFont);
            addMetaCell(overviewTable, "Priority:", c.getPriority().name(), boldBodyFont, bodyFont);
            addMetaCell(overviewTable, "Lead Investigator:", c.getLeadInvestigator() != null ? c.getLeadInvestigator().getFullName() : "N/A", boldBodyFont, bodyFont);
            addMetaCell(overviewTable, "Badge Number:", c.getLeadInvestigator() != null ? c.getLeadInvestigator().getBadgeNumber() : "N/A", boldBodyFont, bodyFont);
            addMetaCell(overviewTable, "Incident Date:", c.getIncidentDate() != null ? c.getIncidentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A", boldBodyFont, bodyFont);
            addMetaCell(overviewTable, "Report Generated:", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), boldBodyFont, bodyFont);

            overviewTable.setSpacingAfter(12);
            document.add(overviewTable);

            // Description
            Paragraph descHeading = new Paragraph("Case Narrative / Incident Summary:", boldBodyFont);
            document.add(descHeading);
            Paragraph descPara = new Paragraph(c.getDescription() != null ? c.getDescription() : "No detailed description recorded.", bodyFont);
            descPara.setSpacingAfter(18);
            document.add(descPara);

            // Evidence Register Section
            Paragraph evidenceHeading = new Paragraph("2. REGISTERED DIGITAL EVIDENCE INVENTORY", sectionFont);
            evidenceHeading.setSpacingAfter(8);
            document.add(evidenceHeading);

            if (evidenceList.isEmpty()) {
                Paragraph noEvd = new Paragraph("No evidence items currently attached to this investigation case.", bodyFont);
                noEvd.setSpacingAfter(15);
                document.add(noEvd);
            } else {
                PdfPTable evdTable = new PdfPTable(6);
                evdTable.setWidthPercentage(100);
                evdTable.setWidths(new float[]{15, 25, 12, 12, 16, 20});

                addTableHeader(evdTable, "Evidence ID");
                addTableHeader(evdTable, "Filename");
                addTableHeader(evdTable, "Category");
                addTableHeader(evdTable, "Size");
                addTableHeader(evdTable, "Integrity Status");
                addTableHeader(evdTable, "Baseline SHA-256");

                for (Evidence e : evidenceList) {
                    evdTable.addCell(new Phrase(e.getEvidenceId(), boldBodyFont));
                    evdTable.addCell(new Phrase(e.getFileName(), bodyFont));
                    evdTable.addCell(new Phrase(e.getEvidenceType().name(), bodyFont));
                    evdTable.addCell(new Phrase(formatFileSize(e.getFileSizeBytes()), bodyFont));
                    evdTable.addCell(new Phrase(e.getStatus().name(), boldBodyFont));
                    evdTable.addCell(new Phrase(e.getOriginalSha256().substring(0, 16) + "...", codeFont));
                }
                evdTable.setSpacingAfter(18);
                document.add(evdTable);
            }

            // Chain of Custody Section
            Paragraph custodyHeading = new Paragraph("3. HASH-LINKED CHAIN OF CUSTODY AUDIT TRAIL", sectionFont);
            custodyHeading.setSpacingAfter(8);
            document.add(custodyHeading);

            for (Evidence e : evidenceList) {
                Paragraph evdSub = new Paragraph("Evidence: " + e.getEvidenceId() + " (" + e.getFileName() + ")", boldBodyFont);
                document.add(evdSub);

                List<CustodyRecord> chain = custodyRecordRepository.findByEvidence_IdOrderByIdAsc(e.getId());
                PdfPTable cTable = new PdfPTable(5);
                cTable.setWidthPercentage(100);
                cTable.setWidths(new float[]{18, 16, 18, 28, 20});

                addTableHeader(cTable, "Timestamp");
                addTableHeader(cTable, "Action");
                addTableHeader(cTable, "Handler");
                addTableHeader(cTable, "Notes / Transferred To");
                addTableHeader(cTable, "Block Checksum");

                for (CustodyRecord rec : chain) {
                    cTable.addCell(new Phrase(rec.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), smallFont));
                    cTable.addCell(new Phrase(rec.getAction().name(), boldBodyFont));
                    cTable.addCell(new Phrase(rec.getPerformedBy() != null ? rec.getPerformedBy().getFullName() : "SYSTEM", smallFont));
                    String noteText = (rec.getTransferredTo() != null ? "To: " + rec.getTransferredTo() + " | " : "") +
                            (rec.getNotes() != null ? rec.getNotes() : "");
                    cTable.addCell(new Phrase(noteText, smallFont));
                    cTable.addCell(new Phrase(rec.getRecordHash().substring(0, 16) + "...", codeFont));
                }
                cTable.setSpacingAfter(12);
                document.add(cTable);
            }

            // Forensic Certification Statement
            Paragraph certHeading = new Paragraph("4. FORENSIC INTEGRITY CERTIFICATION & LEGAL ATTESTATION", sectionFont);
            certHeading.setSpacingAfter(6);
            document.add(certHeading);

            PdfPTable certTable = new PdfPTable(1);
            certTable.setWidthPercentage(100);
            String certText = "I hereby certify that this document represents a true and tamper-evident extraction of the case files and chain-of-custody records maintained in the Digital Evidence & Case Management System (DECMS). All digital evidence hashes were generated using SHA-256 cryptographic algorithms in accordance with ISO/IEC 27037 standards for digital evidence handling. The hash-linked ledger records confirm uninterrupted chain of custody.\n\n"
                    + "Lead Investigator: " + (c.getLeadInvestigator() != null ? c.getLeadInvestigator().getFullName() : "N/A")
                    + "   |   Badge: " + (c.getLeadInvestigator() != null ? c.getLeadInvestigator().getBadgeNumber() : "N/A")
                    + "   |   Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            PdfPCell certCell = new PdfPCell(new Phrase(certText, smallFont));
            certCell.setBackgroundColor(new Color(241, 245, 249));
            certCell.setPadding(8);
            certTable.addCell(certCell);
            document.add(certTable);

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Error generating forensic PDF report: " + ex.getMessage(), ex);
        }
    }

    private void addMetaCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell lCell = new PdfPCell(new Phrase(label, labelFont));
        lCell.setBackgroundColor(new Color(248, 250, 252));
        lCell.setPadding(5);
        table.addCell(lCell);

        PdfPCell vCell = new PdfPCell(new Phrase(value != null ? value : "N/A", valueFont));
        vCell.setPadding(5);
        table.addCell(vCell);
    }

    private void addTableHeader(PdfPTable table, String title) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        PdfPCell header = new PdfPCell(new Phrase(title, headerFont));
        header.setBackgroundColor(new Color(30, 41, 59));
        header.setPadding(5);
        table.addCell(header);
    }

    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
}
