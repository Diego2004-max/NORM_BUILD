package com.normbuild.regulation.service;

import com.normbuild.config.NormBuildAiProperties;
import java.time.Duration;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class LlmClient {

    private final WebClient webClient;
    private final NormBuildAiProperties properties;

    public LlmClient(WebClient.Builder webClientBuilder, NormBuildAiProperties properties) {
        this.webClient = webClientBuilder.build();
        this.properties = properties;
    }

    public String generateChecklist(String prompt) {
        Map<String, Object> request = Map.of(
                "model", properties.modelName(),
                "prompt", prompt,
                "stream", false
        );
        Map<String, Object> response = webClient.post()
                .uri(properties.llmEndpoint())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .timeout(Duration.ofSeconds(properties.llmTimeoutSeconds()))
                .onErrorMap(throwable -> new AiProviderException("El modelo local tardó demasiado en generar la respuesta.", throwable))
                .block();
        Object answer = response == null ? null : response.get("response");
        if (!(answer instanceof String text) || text.isBlank()) {
            throw new AiProviderException("El modelo local devolvió una respuesta vacía.");
        }
        return text.trim();
    }
}
