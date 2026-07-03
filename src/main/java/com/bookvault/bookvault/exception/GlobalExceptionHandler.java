package com.bookvault.bookvault.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

// 📘 CONCEPT: Video 16 - Global Error Handler (final safety net)
// All errors bubble here — single place handles all error formatting
// 🟡 NOVICE: handle errors per-controller → inconsistent format, easy to miss cases
// 🏢 PRODUCT: centralized handler → consistent JSON error structure always
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ── Validation Errors (Video 9) ──────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        // 📘 CONCEPT: Video 18 - Structured logging with context
        log.warn("Validation failed: {}", fieldErrors);

        return ResponseEntity.badRequest().body(
            ErrorResponse.of(400, "Validation failed", fieldErrors)
        );
    }

    // ── Not Found (Video 11 + Video 20) ─────────────────────────────────
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex) {
        log.debug("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse.of(404, ex.getMessage(), null)
        );
    }

    // ── Conflict / Duplicate (Video 12) ─────────────────────────────────
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex) {

        // 📘 CONCEPT: Video 20 - Never leak internal details
        // 🟡 NOVICE: return ex.getMessage() → exposes table names, constraint names
        // 🏢 PRODUCT: check constraint type → return friendly message
        String message = "A resource with this information already exists";

        String exMsg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        if (exMsg.contains("email")) {
            message = "An account with this email already exists";
        } else if (exMsg.contains("isbn")) {
            message = "A book with this ISBN already exists";
        }

        log.warn("Data integrity violation: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            ErrorResponse.of(409, message, null)
        );
    }

    // ── Authentication Errors (Video 8 + Video 20) ───────────────────────
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationError(
            AuthenticationException ex) {

        // 📘 CONCEPT: Video 20 - Generic auth messages
        // 🟡 NOVICE: "User not found" or "Wrong password" → tells attacker which is wrong
        // 🏢 PRODUCT: always "Authentication failed" regardless of which check failed
        //             prevents username enumeration attacks
        log.warn("Authentication failed: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ErrorResponse.of(401, "Authentication failed", null)
        );
    }

    // ── Authorization Errors (Video 20 - BOLA/BFLA) ─────────────────────
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex) {

        // 📘 CONCEPT: Video 20 - 403 → 404 masking
        // 🟡 NOVICE: return 403 Forbidden → tells attacker resource exists
        //             but they lack permission → information leakage
        // 🏢 PRODUCT: return 404 Not Found → attacker can't tell if resource
        //             exists or they just don't have access → no information leak
        log.warn("Access denied - returning 404 to prevent information leakage: {}",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse.of(404, "Resource not found", null)
        );
    }

    // ── Business Logic Errors ────────────────────────────────────────────
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessError(
            BusinessException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(
            ErrorResponse.of(400, ex.getMessage(), null)
        );
    }

    // ── Catch-All (Video 16 - never leak internals) ─────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {

        // 📘 CONCEPT: Video 20 - Never expose internal error details
        // 🟡 NOVICE: return ex.getMessage() → stack traces, class names in response
        //             attackers learn your code structure, plan targeted attacks
        // 🏢 PRODUCT: log full error internally, return generic message to client
        log.error("Unexpected error occurred", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse.of(500, "An unexpected error occurred", null)
        );
    }

    // ── Error Response Structure ─────────────────────────────────────────
    public record ErrorResponse(
            int status,
            String message,
            Map<String, String> errors,
            String timestamp
    ) {
        public static ErrorResponse of(int status, String message,
                Map<String, String> errors) {
            return new ErrorResponse(status, message, errors,
                    OffsetDateTime.now().toString());
        }
    }
}
