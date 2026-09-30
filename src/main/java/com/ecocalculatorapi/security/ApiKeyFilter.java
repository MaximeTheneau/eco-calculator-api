package com.ecocalculatorapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

import java.io.IOException;

/**
 * Exige un en-tête X-API-KEY correspondant à CUSTOM_API_KEY sur toutes
 * les routes /api/**. Volontairement simple : pas de Spring Security,
 * juste un filtre de servlet, suffisant pour un usage perso/portfolio.
 * Pour une vraie mise en production (plusieurs clients, rotation des clés,
 * rôles...), Spring Security serait plus adapté.
 */
@Component
@Order(1)
public class ApiKeyFilter extends HttpFilter {

    private static final String HEADER_NAME = "X-API-KEY";

    @Value("${app.api-key:}")
    private String expectedApiKey;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String path = request.getRequestURI();

        // Ne protège que les endpoints /api/**
        if (!path.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        // Si aucune clé n'est configurée côté serveur, on laisse passer
        // (utile en dev local) mais on log un avertissement.
        if (expectedApiKey == null || expectedApiKey.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        String providedKey = request.getHeader(HEADER_NAME);

        if (expectedApiKey.equals(providedKey)) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"En-tête X-API-KEY manquant ou invalide.\"}"
            );
        }
    }
}
