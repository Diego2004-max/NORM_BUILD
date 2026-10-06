package com.normbuild.regulation.service;

import java.util.Optional;

public record ProjectFacts(
        Optional<Integer> floors,
        Optional<Double> heightMeters,
        Optional<Double> areaSquareMeters,
        boolean mentionsResidentialUse,
        boolean mentionsSetbacks,
        boolean mentionsOccupancyIndex,
        boolean mentionsVolumetry
) {
}
