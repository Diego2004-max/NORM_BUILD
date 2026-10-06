package com.normbuild.regulation.service;

import com.normbuild.regulation.dto.ComplianceQueryRequest;
import com.normbuild.regulation.repository.RegulatoryDocumentProjection;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RegulatoryPromptBuilder {

    public String buildChecklistPrompt(ComplianceQueryRequest request, List<RegulatoryDocumentProjection> documents) {
        StringBuilder builder = new StringBuilder();
        builder.append("You are NormBuild, a strict building regulation compliance assistant.\n");
        builder.append("Answer only in Spanish. Use only the provided regulatory context. ");
        builder.append("If the context is insufficient, say exactly which missing official document is required.\n\n");
        builder.append("Citizen project description:\n");
        builder.append(request.projectDescription()).append("\n\n");
        builder.append("Regulatory context:\n");
        for (int index = 0; index < documents.size(); index++) {
            RegulatoryDocumentProjection document = documents.get(index);
            builder.append("Source ").append(index + 1).append(": ");
            builder.append(document.getTitle()).append(" ");
            builder.append(document.getRegulationCode()).append(" ");
            builder.append(document.getArticleReference()).append("\n");
            builder.append(document.getContent()).append("\n\n");
        }
        builder.append("Return a practical checklist with steps, evidence required and cited articles. ");
        builder.append("Distinguish lot area from built area. Do not invent numeric limits or authorize construction. ");
        builder.append("Semantic similarity is not compliance evidence. Report risk as 'Por verificar' until parcel-specific rules are verified.");
        return builder.toString();
    }
}
