package com.decms.service;

import com.decms.domain.model.custody.CustodyAction;
import com.decms.domain.model.custody.CustodyRecord;
import com.decms.domain.model.evidence.Evidence;
import com.decms.domain.model.user.User;
import com.decms.dto.CustodyDTO;
import com.decms.repository.CustodyRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustodyLedgerService {

    private static final String GENESIS_PREV_HASH = "0000000000000000000000000000000000000000000000000000000000000000";

    private final CustodyRecordRepository custodyRecordRepository;

    public CustodyLedgerService(CustodyRecordRepository custodyRecordRepository) {
        this.custodyRecordRepository = custodyRecordRepository;
    }

    @Transactional
    public CustodyRecord recordCustodyEvent(Evidence evidence, User performedBy, CustodyAction action,
                                           String transferredTo, String notes) {
        Optional<CustodyRecord> latestOpt = custodyRecordRepository.findTopByEvidence_IdOrderByIdDesc(evidence.getId());
        String previousHash = latestOpt.map(CustodyRecord::getRecordHash).orElse(GENESIS_PREV_HASH);

        CustodyRecord newRecord = new CustodyRecord(evidence, performedBy, action, transferredTo, notes, previousHash);
        return custodyRecordRepository.save(newRecord);
    }

    public List<CustodyDTO.CustodyRecordDTO> getCustodyHistory(Long evidenceId) {
        List<CustodyRecord> records = custodyRecordRepository.findByEvidence_IdOrderByIdAsc(evidenceId);
        List<CustodyDTO.CustodyRecordDTO> dtos = new ArrayList<>();
        for (CustodyRecord r : records) {
            dtos.add(mapToDTO(r));
        }
        return dtos;
    }

    public List<CustodyDTO.CustodyRecordDTO> getAllCustodyRecords() {
        List<CustodyRecord> records = custodyRecordRepository.findAllByOrderByIdDesc();
        List<CustodyDTO.CustodyRecordDTO> dtos = new ArrayList<>();
        for (CustodyRecord r : records) {
            dtos.add(mapToDTO(r));
        }
        return dtos;
    }

    public CustodyDTO.ChainValidationResultDTO validateEvidenceChain(Evidence evidence) {
        List<CustodyRecord> chain = custodyRecordRepository.findByEvidence_IdOrderByIdAsc(evidence.getId());
        CustodyDTO.ChainValidationResultDTO result = new CustodyDTO.ChainValidationResultDTO();
        result.setEvidenceId(evidence.getEvidenceId());
        result.setTotalBlocks(chain.size());

        List<String> issues = new ArrayList<>();
        int validCount = 0;
        String expectedPrevHash = GENESIS_PREV_HASH;

        for (int i = 0; i < chain.size(); i++) {
            CustodyRecord block = chain.get(i);
            boolean blockValid = true;

            // 1. Verify previous hash pointer
            if (!block.getPreviousRecordHash().equalsIgnoreCase(expectedPrevHash)) {
                issues.add(String.format("Block #%d hash pointer mismatch. Expected: %s, Found: %s",
                        block.getId(), expectedPrevHash, block.getPreviousRecordHash()));
                blockValid = false;
            }

            // 2. Verify block checksum recalculation
            String recalculated = block.calculateBlockChecksum();
            if (!recalculated.equalsIgnoreCase(block.getRecordHash())) {
                issues.add(String.format("Block #%d content checksum mismatch. Recorded: %s, Recalculated: %s",
                        block.getId(), block.getRecordHash(), recalculated));
                blockValid = false;
            }

            if (blockValid) {
                validCount++;
            }
            expectedPrevHash = block.getRecordHash();
        }

        result.setValidBlocks(validCount);
        result.setIssues(issues);
        result.setEntireChainValid(issues.isEmpty() && validCount == chain.size());
        return result;
    }

    private CustodyDTO.CustodyRecordDTO mapToDTO(CustodyRecord record) {
        CustodyDTO.CustodyRecordDTO dto = new CustodyDTO.CustodyRecordDTO();
        dto.setId(record.getId());
        dto.setEvidenceId(record.getEvidence() != null ? record.getEvidence().getEvidenceId() : "N/A");
        dto.setEvidenceFileName(record.getEvidence() != null ? record.getEvidence().getFileName() : "N/A");
        dto.setPerformedByUsername(record.getPerformedBy() != null ? record.getPerformedBy().getUsername() : "SYSTEM");
        dto.setPerformedByFullName(record.getPerformedBy() != null ? record.getPerformedBy().getFullName() : "System Automated");
        dto.setAction(record.getAction());
        dto.setTransferredTo(record.getTransferredTo());
        dto.setNotes(record.getNotes());
        dto.setPreviousRecordHash(record.getPreviousRecordHash());
        dto.setRecordHash(record.getRecordHash());
        dto.setBlockIntegrityValid(record.verifyBlockIntegrity());
        dto.setTimestamp(record.getTimestamp());
        return dto;
    }
}
