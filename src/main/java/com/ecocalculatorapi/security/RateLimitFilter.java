package com.ecocalculatorapi.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limite le nombre de requêtes à la fois par adresse IP et par clé API.
 * Une requête est bloquée dès que l'une des deux limites est atteinte.
 * - Limite par IP : protège même les requêtes sans clé valide (ex. tentative
 *   de deviner la clé API par force brute).
 * - Limite par clé : protège contre un usage abusif d'une clé compromise
 *   ou trop sollicitée, quelle que soit l'IP d'origine.
 * S'exécute APRÈS ApiKeyFilter (@Order(2) > @Order(1) sur ApiKeyFilter).
 */
@Component
@Order(2)
public class RateLimitFilter extends HttpFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";

    // Stockage en mémoire : une bucket par IP, une autre par clé API.
    // Pour plusieurs instances de l'API, il faudrait un stockage partagé (Redis).
    private final ConcurrentHashMap<String, Bucket> ipBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> keyBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String path = request.getRequestURI();
        if (!path.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = extractClientIp(request);
        Bucket ipBucket = ipBuckets.computeIfAbsent(clientIp, ip -> newIpBucket());

        if (!ipBucket.tryConsume(1)) {
            rejectWithTooManyRequests(response, "Limite de requêtes atteinte pour cette adresse IP.");
            return;
        }

        String apiKey = request.getHeader(API_KEY_HEADER);
        if (apiKey != null && !apiKey.isBlank()) {
            Bucket keyBucket = keyBuckets.computeIfAbsent(apiKey, key -> newKeyBucket());
            if (!keyBucket.tryConsume(1)) {
                rejectWithTooManyRequests(response, "Limite de requêtes atteinte pour cette clé API.");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private Bucket newIpBucket() {
        // Limite large : couvre les requêtes sans clé (bloquées ensuite par
        // ApiKeyFilter) et empêche le brute-force de la clé.
        Bandwidth limit = Bandwidth.builder()
                .capacity(60)
                .refillGreedy(60, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket newKeyBucket() {
        // Limite plus stricte, propre à chaque clé API valide.
        Bandwidth limit = Bandwidth.builder()
                .capacity(30)
                .refillGreedy(30, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private void rejectWithTooManyRequests(HttpServletResponse response, String message) throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", "60");
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"" + message + "\"}"
        );
    }

    /**
     * Récupère l'IP réelle du client. Derrière un reverse proxy (Nginx,
     * Traefik...), l'IP directe serait celle du proxy : on regarde d'abord
     * X-Forwarded-For si présent.
     */
    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
