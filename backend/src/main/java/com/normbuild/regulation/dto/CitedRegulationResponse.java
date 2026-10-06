package com.normbuild.regulation.dto;

import java.util.UUID;

public record CitedRegulationResponse(
        UUID id,
        String title,
        String regulationCode,
        String articleReference,
        double similarity,
        String excerpt
) {
}
