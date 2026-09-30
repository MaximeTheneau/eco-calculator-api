package com.ecocalculatorapi.exception;

import com.ecocalculatorapi.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * Intercepte les exceptions levées par les contrôleurs et renvoie
 * une réponse JSON homogène (ErrorResponse) au lieu de la page
 * Whitelabel par défaut ou d'une stack trace brute.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // URL mal formée ou vide (ex : "pas-une-url")
    @ExceptionHandler({MalformedURLException.class, URISyntaxException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadUrl(Exception ex, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "URL invalide : " + ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(body);
    }

    // Site injoignable, timeout, DNS introuvable...
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleUnreachableSite(IOException ex, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_GATEWAY.value(),
                "Bad Gateway",
                "Impossible de récupérer la page : " + ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    // Requête interrompue (timeout applicatif)
    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<ErrorResponse> handleInterrupted(InterruptedException ex, HttpServletRequest request) {
        Thread.currentThread().interrupt(); // bonne pratique : restaure le flag d'interruption
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.GATEWAY_TIMEOUT.value(),
                "Gateway Timeout",
                "La requête vers le site cible a été interrompue.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(body);
    }

    // Filet de sécurité pour toute autre erreur imprévue
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAnyOtherError(Exception ex, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Une erreur inattendue est survenue.",
                request.getRequestURI()
        );
        return ResponseEntity.internalServerError().body(body);
    }
}
