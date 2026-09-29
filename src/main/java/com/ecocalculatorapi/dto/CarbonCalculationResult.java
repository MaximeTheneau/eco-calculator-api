package com.ecocalculatorapi.dto;

/**
 * Résultat d'un calcul d'empreinte carbone pour une page web.
 */
public record CarbonCalculationResult(
        String url,
        long pageSizeBytes,
        double co2GramsPerVisit,
        double co2GramsPerYear, // en supposant ~10 000 visites/mois, comme Website Carbon
        boolean greenHosting,
        String rating // A+, A, B, C, D, E, F
) {
}
