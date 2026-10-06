package com.normbuild.regulation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.normbuild.config.NormBuildAiProperties;
import com.normbuild.regulation.dto.ChecklistGenerationMode;
import com.normbuild.regulation.dto.ComplianceChecklistResponse;
import com.normbuild.regulation.dto.ComplianceQueryRequest;
import com.normbuild.regulation.repository.RegulatoryDocumentJdbcRepository;
import com.normbuild.regulation.repository.RegulatoryDocumentProjection;
import com.normbuild.regulation.repository.RegulatoryDocumentRepository;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegulatoryRagServiceTest {

    private final EmbeddingClient embeddingClient = mock(EmbeddingClient.class);
    private final LlmClient llmClient = mock(LlmClient.class);
    private final RegulatoryDocumentRepository repository = mock(RegulatoryDocumentRepository.class);
    private final ComplianceQueryRequest request = new ComplianceQueryRequest(
            "Bogotá D.C.", "Casa de dos pisos con altura total de 7.5 metros en un lote de 180 m².");
    private RegulatoryRagService service;

    @BeforeEach
    void setUp() {
        NormBuildAiProperties properties = new NormBuildAiProperties(
                768, "http://localhost:11434/api/generate", "http://localhost:11434/api/embeddings",
                "llama3.1", "nomic-embed-text", 0.35, 5, 30, 18);
        service = new RegulatoryRagService(properties, embeddingClient, llmClient,
                new EmbeddingFormatter(), new RegulatoryPromptBuilder(),
                new DeterministicChecklistBuilder(new ProjectFactExtractor()),
                mock(DocumentParsingService.class), repository,
                mock(RegulatoryDocumentJdbcRepository.class), Runnable::run);
        when(embeddingClient.createEmbedding(anyString())).thenReturn(List.of(0.2, 0.8));
        RegulatoryDocumentProjection document = source();
        when(repository.findNearestRegulations(anyString(), anyString(), anyDouble(), anyInt()))
                .thenReturn(List.of(document));
    }

    @Test
    void reportsGuidedModeWhenGenerationFailsAndKeepsCitations() {
        when(llmClient.generateChecklist(anyString())).thenThrow(new AiProviderException("Modelo no disponible."));

        ComplianceChecklistResponse response = service.answerComplianceQuestion(request).join();

        assertThat(response.generationMode()).isEqualTo(ChecklistGenerationMode.GUIDED);
        assertThat(response.answer()).contains("Número de pisos: 2", "Altura propuesta: 7.5 m", "180 m²",
                "Nivel de riesgo: Por verificar");
        assertThat(response.answer()).doesNotContain("Medio-Alto", "POT de Bogotá");
        assertThat(response.citations()).hasSize(1);
        assertThat(response.riskLevel()).isEqualTo("Por verificar");
    }

    @Test
    void doesNotTranslateHighSimilarityIntoLowComplianceRisk() {
        when(llmClient.generateChecklist(anyString())).thenReturn("Verifica la norma específica del predio.");

        ComplianceChecklistResponse response = service.answerComplianceQuestion(request).join();

        assertThat(response.generationMode()).isEqualTo(ChecklistGenerationMode.GENERATIVE);
        assertThat(response.citations().getFirst().similarity()).isEqualTo(0.99);
        assertThat(response.riskLevel()).isEqualTo("Por verificar");
        verify(llmClient).generateChecklist(anyString());
    }

    @Test
    void doesNotGenerateWithoutRetrievedSources() {
        when(repository.findNearestRegulations(anyString(), anyString(), anyDouble(), anyInt())).thenReturn(List.of());

        ComplianceChecklistResponse response = service.answerComplianceQuestion(request).join();

        assertThat(response.generationMode()).isEqualTo(ChecklistGenerationMode.NO_CONTEXT);
        assertThat(response.citations()).isEmpty();
        verify(llmClient, never()).generateChecklist(anyString());
    }

    @Test
    void propagatesEmbeddingFailureInsteadOfFabricatingSources() {
        when(embeddingClient.createEmbedding(anyString())).thenThrow(new AiProviderException("Vector no disponible."));

        assertThatThrownBy(() -> service.answerComplianceQuestion(request).join())
                .isInstanceOf(CompletionException.class).hasCauseInstanceOf(AiProviderException.class);
        verify(repository, never()).findNearestRegulations(anyString(), anyString(), anyDouble(), anyInt());
        verify(llmClient, never()).generateChecklist(anyString());
    }

    private RegulatoryDocumentProjection source() {
        RegulatoryDocumentProjection source = mock(RegulatoryDocumentProjection.class);
        when(source.getId()).thenReturn(UUID.fromString("b6180b28-0944-4e94-a612-dbd0b56c04db"));
        when(source.getTitle()).thenReturn("Documento de prueba");
        when(source.getRegulationCode()).thenReturn("Reglamento de prueba");
        when(source.getArticleReference()).thenReturn("Referencia de prueba");
        when(source.getContent()).thenReturn("Confirma la norma específica del predio.");
        when(source.getSimilarity()).thenReturn(0.99);
        return source;
    }
}
