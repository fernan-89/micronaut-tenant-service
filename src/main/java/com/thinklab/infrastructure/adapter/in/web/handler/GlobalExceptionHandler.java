package com.thinklab.infrastructure.adapter.in.web.handler;

import com.thinklab.domain.exception.BusinessException;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global Exception Handler: The centralized safety net for the Administrative Center API.
 * This adapter intercepts all exceptions traversing the reactive pipeline and translates
 * them into standardized RFC 7807 "Problem Details" responses.
 *
 * <p>It serves as an Anti-Corruption Layer for error signaling, ensuring that domain
 * business rules and technical infrastructure failures are projected with high fidelity
 * while preventing sensitive internal data leakage.</p>
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 *     <li><b>Contractual Consistency:</b> Guarantees a predictable error payload structure for all consumers.</li>
 *     <li><b>Protocol Decoupling:</b> Maps domain-specific ErrorCodes to appropriate HTTP Status Codes.</li>
 *     <li><b>Observability:</b> Structured logging of root causes to facilitate rapid incident response (SRE).</li>
 * </ul>
 */
@Slf4j
@Produces
@Singleton
@Requires(classes = {ExceptionHandler.class})
public class GlobalExceptionHandler implements ExceptionHandler<Throwable, HttpResponse<Map<String, Object>>> {

    /**
     * Handles and transforms any Throwable caught at the system boundary.
     *
     * @param request   The current HTTP request context.
     * @param exception The caught exception.
     * @return A standardized {@link HttpResponse} containing the structured error payload.
     */
    @Override
    public HttpResponse<Map<String, Object>> handle(HttpRequest request, Throwable exception) {
        log.error("[ACTION: GLOBAL_EXCEPTION_HANDLER] - Exception captured at boundary: {} | Path: [{}]",
                exception.getMessage(), request.getPath());

        if (exception instanceof BusinessException businessEx) {
            return handleBusinessException(businessEx);
        }

        if (exception instanceof ConstraintViolationException constraintEx) {
            return handleValidationException(constraintEx);
        }

        return handleGenericException(exception);
    }

    /**
     * Maps Domain Business Exceptions to granular 4xx status codes.
     */
    private HttpResponse<Map<String, Object>> handleBusinessException(BusinessException ex) {
        HttpStatus status = switch (ex.getErrorCode()) {
            case "TENANT_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "TENANT_DUPLICATE", "INVALID_LIFECYCLE_TRANSITION" -> HttpStatus.CONFLICT;
            default -> HttpStatus.UNPROCESSABLE_ENTITY;
        };

        log.warn("[ACTION: DOMAIN_EXCEPTION] [CODE: {}] - Business rule violation: {}", ex.getErrorCode(), ex.getMessage());
        return HttpResponse.status(status).body(createProblem(ex.getErrorCode(), ex.getMessage(), status));
    }

    /**
     * Maps Jakarta Validation violations to HTTP 400 Bad Request.
     */
    private HttpResponse<Map<String, Object>> handleValidationException(ConstraintViolationException ex) {
        String details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));

        log.warn("[ACTION: VALIDATION_EXCEPTION] - Syntactic contract violation: {}", details);
        return HttpResponse.badRequest(createProblem("BAD_REQUEST", details, HttpStatus.BAD_REQUEST));
    }

    /**
     * Maps unexpected technical failures to HTTP 500 Internal Server Error.
     * Injects the Root Cause into logs for SRE analysis but masks it for external clients.
     */
    private HttpResponse<Map<String, Object>> handleGenericException(Throwable ex) {
        log.error("[ACTION: INTERNAL_FAILURE] - Critical technical failure detected:", ex);

        return HttpResponse.serverError(createProblem(
                "INTERNAL_SERVER_ERROR",
                "An unexpected technical failure occurred. Please contact system support.",
                HttpStatus.INTERNAL_SERVER_ERROR
        ));
    }

    /**
     * Factory method for creating the standardized RFC 7807 error payload.
     */
    private Map<String, Object> createProblem(String code, String message, HttpStatus status) {
        Map<String, Object> problem = new LinkedHashMap<>();
        problem.put("timestamp", Instant.now().toString());
        problem.put("status", status.getCode());
        problem.put("error_code", code);
        problem.put("message", message);
        return problem;
    }
}