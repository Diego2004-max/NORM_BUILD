package com.normbuild.regulation.service;

import com.normbuild.config.NormBuildAiProperties;
import com.normbuild.regulation.dto.CitedRegulationResponse;
import com.normbuild.regulation.dto.ComplianceChecklistResponse;
import com.normbuild.regulation.dto.ComplianceQueryRequest;
import com.normbuild.regulation.dto.DocumentIngestionRequest;
import com.normbuild.regulation.dto.DocumentIngestionResponse;
import com.normbuild.regulation.repository.RegulatoryDocumentJdbcRepository;
import com.normbuild.regulation.repository.RegulatoryDocumentProjection;
import com.normbuild.regulation.repository.RegulatoryDocumentRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegulatoryRagService {

    private final NormBuildAiProperties properties;
    private final EmbeddingClient embeddingClient;
    private final LlmClient llmClient;
    private final EmbeddingFormatter embeddingFormatter;
    private final RegulatoryPromptBuilder promptBuilder;
    private final DocumentParsingService parsingService;
    private final RegulatoryDocumentRepository documentRepository;
    private final RegulatoryDocumentJdbcRepository jdbcRepository;
    private final Executor ragTaskExecutor;

    public RegulatoryRagService(
            NormBuildAiProperties properties,
            EmbeddingClient embeddingClient,
            LlmClient llmClient,
            EmbeddingFormatter embeddingFormatter,
            RegulatoryPromptBuilder promptBuilder,
            DocumentParsingService parsingService,
            RegulatoryDocumentRepository documentRepository,
            RegulatoryDocumentJdbcRepository jdbcRepository,
            @Qualifier("ragTaskExecutor") Executor ragTaskExecutor
    ) {
        this.properties = properties;
        this.embeddingClient = embeddingClient;
        this.llmClient = llmClient;
        this.embeddingFormatter = embeddingFormatter;
        this.promptBuilder = promptBuilder;
        this.parsingService = parsingService;
        this.documentRepository = documentRepository;
        this.jdbcRepository = jdbcRepository;
        this.ragTaskExecutor = ragTaskExecutor;
    }

    public CompletableFuture<ComplianceChecklistResponse> answerComplianceQuestion(ComplianceQueryRequest request) {
        CompletableFuture<List<Double>> embeddingFuture = CompletableFuture.supplyAsync(
                () -> embeddingClient.createEmbedding(request.projectDescription()),
                ragTaskExecutor
        );
        CompletableFuture<List<RegulatoryDocumentProjection>> contextFuture = embeddingFuture.thenApplyAsync(embedding -> {
            String vectorLiteral = embeddingFormatter.toVectorLiteral(embedding);
            return documentRepository.findNearestRegulations(
                    request.jurisdiction(),
                    vectorLiteral,
                    properties.similarityThreshold(),
                    properties.maxContextResults()
            );
        }, ragTaskExecutor);
        return contextFuture.thenApplyAsync(context -> {
            if (context.isEmpty()) {
                return new ComplianceChecklistResponse(
                        "No encontré normativa oficial suficiente para responder con seguridad. Carga documentos oficiales de la jurisdicción indicada antes de emitir un concepto.",
                        "Alto",
                        List.of(),
                        OffsetDateTime.now()
                );
            }
            String prompt = promptBuilder.buildChecklistPrompt(request, context);
            String answer = llmClient.generateChecklist(prompt);
            return new ComplianceChecklistResponse(answer, estimateRiskLevel(context), mapCitations(context), OffsetDateTime.now());
        }, ragTaskExecutor);
    }

    @Transactional
    public CompletableFuture<DocumentIngestionResponse> ingestDocument(DocumentIngestionRequest request) {
        CompletableFuture<String> normalizedFuture = parsingService.normalizeContentAsync(request.content());
        CompletableFuture<String> summaryFuture = parsingService.buildSearchSummaryAsync(request.content());
        return normalizedFuture.thenCombineAsync(summaryFuture, (normalized, summary) -> {
            List<Double> embedding = embeddingClient.createEmbedding(summary + "\n" + normalized);
            UUID documentId = UUID.randomUUID();
            DocumentIngestionRequest normalizedRequest = new DocumentIngestionRequest(
                    request.title(),
                    request.jurisdiction(),
                    request.regulationCode(),
                    request.articleReference(),
                    normalized
            );
            jdbcRepository.insertDocument(documentId, normalizedRequest, embeddingFormatter.toVectorLiteral(embedding));
            return new DocumentIngestionResponse(documentId, "INDEXED");
        }, ragTaskExecutor);
    }

    private List<CitedRegulationResponse> mapCitations(List<RegulatoryDocumentProjection> documents) {
        return documents.stream()
                .map(document -> new CitedRegulationResponse(
                        document.getId(),
                        document.getTitle(),
                        document.getRegulationCode(),
                        document.getArticleReference(),
                        document.getSimilarity(),
                        excerpt(document.getContent())
                ))
                .toList();
    }

    private String estimateRiskLevel(List<RegulatoryDocumentProjection> documents) {
        double bestSimilarity = documents.stream()
                .mapToDouble(RegulatoryDocumentProjection::getSimilarity)
                .max()
                .orElse(0.0);
        if (bestSimilarity >= 0.86) {
            return "Bajo";
        }
        if (bestSimilarity >= 0.78) {
            return "Medio";
        }
        return "Alto";
    }

    private String excerpt(String content) {
        int maxLength = Math.min(content.length(), 320);
        return content.substring(0, maxLength);
    }
}
