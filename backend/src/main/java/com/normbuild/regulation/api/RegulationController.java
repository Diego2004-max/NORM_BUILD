package com.normbuild.regulation.api;

import com.normbuild.regulation.dto.ComplianceChecklistResponse;
import com.normbuild.regulation.dto.ComplianceQueryRequest;
import com.normbuild.regulation.dto.DocumentIngestionRequest;
import com.normbuild.regulation.dto.DocumentIngestionResponse;
import com.normbuild.regulation.service.RegulatoryRagService;
import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/regulations")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class RegulationController {

    private final RegulatoryRagService ragService;

    public RegulationController(RegulatoryRagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/compliance-checklist")
    public CompletableFuture<ComplianceChecklistResponse> createComplianceChecklist(@Valid @RequestBody ComplianceQueryRequest request) {
        return ragService.answerComplianceQuestion(request);
    }

    @PostMapping("/documents")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CompletableFuture<DocumentIngestionResponse> ingestDocument(@Valid @RequestBody DocumentIngestionRequest request) {
        return ragService.ingestDocument(request);
    }
}
