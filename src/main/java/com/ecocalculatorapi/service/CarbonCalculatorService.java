package com.ecocalculatorapi.service;

import com.ecocalculatorapi.dto.CarbonCalculationResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Calcule une estimation de l'empreinte carbone d'une page web,
 * inspirée de la méthodologie publique "Sustainable Web Design"
 * (sustainablewebdesign.org), utilisée notamment par Website Carbon.
 * Les constantes ci-dessous sont des moyennes approximatives de l'industrie,
 * pas une reproduction exacte d'un outil existant.
 */
@Service
public class CarbonCalculatorService {

    private static final double KWH_PER_GB = 1.805;
    private static final double GLOBAL_GRID_INTENSITY = 442.0; // g CO2 / kWh
    private static final double GREEN_HOSTING_MULTIPLIER = 0.86;
    private static final long ASSUMED_MONTHLY_VISITS = 10_000;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public CarbonCalculationResult calculate(String url, boolean greenHosting) throws IOException, InterruptedException {
        long pageSizeBytes = fetchPageSizeBytes(url);
        double co2PerVisit = computeCo2Grams(pageSizeBytes, greenHosting);
        double co2PerYear = co2PerVisit * ASSUMED_MONTHLY_VISITS * 12;
        String rating = computeRating(co2PerVisit);

        return new CarbonCalculationResult(url, pageSizeBytes, co2PerVisit, co2PerYear, greenHosting, rating);
    }

    private long fetchPageSizeBytes(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        // Limite : ne mesure que le document HTML principal, pas les images/CSS/JS.
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        return response.body().length;
    }

    private double computeCo2Grams(long bytes, boolean greenHosting) {
        double gigabytes = bytes / 1_000_000_000.0;
        double energyKwh = gigabytes * KWH_PER_GB;
        double co2Grams = energyKwh * GLOBAL_GRID_INTENSITY;

        if (greenHosting) {
            co2Grams *= GREEN_HOSTING_MULTIPLIER;
        }
        return co2Grams;
    }

    private String computeRating(double co2GramsPerVisit) {
        if (co2GramsPerVisit < 0.095) return "A+";
        if (co2GramsPerVisit < 0.186) return "A";
        if (co2GramsPerVisit < 0.341) return "B";
        if (co2GramsPerVisit < 0.493) return "C";
        if (co2GramsPerVisit < 0.656) return "D";
        if (co2GramsPerVisit < 0.846) return "E";
        return "F";
    }
}
