package com.normbuild.regulation.service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class ProjectFactExtractor {

    private static final Pattern FLOORS_PATTERN = Pattern.compile("(\\d+)\\s*(pisos?|niveles?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern HEIGHT_PATTERN = Pattern.compile("(\\d+(?:[\\.,]\\d+)?)\\s*(metros?|m)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern AREA_PATTERN = Pattern.compile("(\\d+(?:[\\.,]\\d+)?)\\s*(metros?\\s*cuadrados|m2|m²)", Pattern.CASE_INSENSITIVE);

    public ProjectFacts extract(String description) {
        String normalized = description == null ? "" : description.toLowerCase();
        return new ProjectFacts(
                extractInteger(FLOORS_PATTERN, normalized),
                extractDecimal(HEIGHT_PATTERN, normalized),
                extractDecimal(AREA_PATTERN, normalized),
                containsAny(normalized, "residencial", "vivienda", "casa", "bifamiliar", "unifamiliar"),
                containsAny(normalized, "retiro", "retiros", "aislamiento", "aislamientos", "antejardín", "antejardin"),
                containsAny(normalized, "ocupación", "ocupacion", "índice de ocupación", "indice de ocupacion"),
                containsAny(normalized, "volumetría", "volumetria", "altura", "pisos")
        );
    }

    private Optional<Integer> extractInteger(Pattern pattern, String value) {
        Matcher matcher = pattern.matcher(value);
        if (!matcher.find()) {
            return Optional.empty();
        }
        return Optional.of(Integer.parseInt(matcher.group(1)));
    }

    private Optional<Double> extractDecimal(Pattern pattern, String value) {
        Matcher matcher = pattern.matcher(value);
        if (!matcher.find()) {
            return Optional.empty();
        }
        return Optional.of(Double.parseDouble(matcher.group(1).replace(',', '.')));
    }

    private boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }
}
