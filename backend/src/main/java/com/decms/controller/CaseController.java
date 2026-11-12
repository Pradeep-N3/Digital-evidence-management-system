package com.decms.controller;

import com.decms.domain.model.user.User;
import com.decms.dto.CaseDTO;
import com.decms.service.AuthService;
import com.decms.service.CaseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cases")
public class CaseController {

    private final CaseService caseService;
    private final AuthService authService;

    public CaseController(CaseService caseService, AuthService authService) {
        this.caseService = caseService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<CaseDTO.CaseResponseDTO>> getAllCases() {
        return ResponseEntity.ok(caseService.getAllCases());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INVESTIGATOR', 'ADMIN')")
    public ResponseEntity<CaseDTO.CaseResponseDTO> createCase(@Valid @RequestBody CaseDTO.CreateCaseRequest request) {
        User leadInvestigator = authService.getCurrentlyAuthenticatedUser();
        return ResponseEntity.ok(caseService.createCase(request, leadInvestigator));
    }

    @GetMapping("/{caseId}")
    public ResponseEntity<CaseDTO.CaseResponseDTO> getCaseById(@PathVariable String caseId) {
        return ResponseEntity.ok(caseService.getCaseById(caseId));
    }

    @PatchMapping("/{caseId}/status")
    @PreAuthorize("hasAnyRole('INVESTIGATOR', 'ADMIN')")
    public ResponseEntity<CaseDTO.CaseResponseDTO> updateCaseStatus(@PathVariable String caseId,
                                                                    @Valid @RequestBody CaseDTO.UpdateStatusRequest request) {
        User actor = authService.getCurrentlyAuthenticatedUser();
        return ResponseEntity.ok(caseService.updateCaseStatus(caseId, request.getStatus(), actor));
    }
}
