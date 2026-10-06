package com.normbuild.regulation.service;

import com.normbuild.regulation.repository.RegulatoryDocumentJdbcRepository;
import com.normbuild.regulation.repository.RegulatoryDocumentRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class RegulatoryKnowledgeSeeder implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegulatoryKnowledgeSeeder.class);

    private final SeedRegulationCatalog seedCatalog;
    private final RegulatoryDocumentRepository documentRepository;
    private final RegulatoryDocumentJdbcRepository jdbcRepository;
    private final EmbeddingClient embeddingClient;
    private final EmbeddingFormatter embeddingFormatter;

    public RegulatoryKnowledgeSeeder(
            SeedRegulationCatalog seedCatalog,
            RegulatoryDocumentRepository documentRepository,
            RegulatoryDocumentJdbcRepository jdbcRepository,
            EmbeddingClient embeddingClient,
            EmbeddingFormatter embeddingFormatter
    ) {
        this.seedCatalog = seedCatalog;
        this.documentRepository = documentRepository;
        this.jdbcRepository = jdbcRepository;
        this.embeddingClient = embeddingClient;
        this.embeddingFormatter = embeddingFormatter;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (documentRepository.count() > 0) {
            return;
        }
        List<SeedRegulationDocument> documents = seedCatalog.bogotaDocuments();
        LOGGER.info("Indexing {} baseline regulatory documents", documents.size());
        for (SeedRegulationDocument document : documents) {
            try {
                String embeddingLiteral = embeddingFormatter.toVectorLiteral(embeddingClient.createEmbedding(document.content()));
                jdbcRepository.insertDocument(document.id(), document.toIngestionRequest(), embeddingLiteral);
            } catch (RuntimeException exception) {
                LOGGER.warn("Could not index baseline regulatory document '{}': {}", document.title(), exception.getMessage());
            }
        }
    }
}
