package com.thinklab.domain.exception;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Objects;

/**
 * Domain Exception: Base abstract class for all organizational business rule violations.
 * This exception serves as the primary boundary signaling mechanism, differentiating
 * logical domain errors (e.g., state violations, duplicates) from technological
 * infrastructure failures.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 *     <li><b>Standardized Categorization:</b> Enforces the use of ErrorCodes for precise client-side handling.</li>
 *     <li><b>Exception Chaining:</b> Supports preserving original root causes for forensic traceability.</li>
 *     <li><b>Defensive Instantiation:</b> Guarantees successful instantiation even if provided with corrupted metadata.</li>
 *     <li><b>Domain Isolation:</b> Strictly decoupled from HTTP statuses or framework-specific structures.</li>
 * </ul>
 */
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private static final String DEFAULT_ERROR_CODE = "GENERIC_BUSINESS_ERROR";
    private static final String DEFAULT_MESSAGE = "A business invariant was violated during orchestration.";

    /**
     * Constructs a new BusinessException with a specific code and detail message.
     *
     * @param errorCode A standardized string code used for machine categorization (e.g., "TENANT_NOT_FOUND").
     * @param message   The human-readable description of the specific business violation.
     */
    public BusinessException(@Nullable String errorCode, @Nullable String message) {
        super(message == null || message.isBlank() ? DEFAULT_MESSAGE : message);
        this.errorCode = errorCode == null || errorCode.isBlank() ? DEFAULT_ERROR_CODE : errorCode;
    }

    /**
     * Constructs a new BusinessException preserving the original system cause.
     *
     * @param errorCode A standardized machine-readable error category.
     * @param message   The business description of the failure.
     * @param cause     The underlying Throwable that triggered this domain exception.
     */
    public BusinessException(@Nullable String errorCode, @Nullable String message, @Nullable Throwable cause) {
        super(message == null || message.isBlank() ? DEFAULT_MESSAGE : message, cause);
        this.errorCode = errorCode == null || errorCode.isBlank() ? DEFAULT_ERROR_CODE : errorCode;
    }

    /**
     * Retrieves the standardized error code associated with this business violation.
     *
     * @return The error code string used for contract-based client mapping.
     */
    @Nonnull
    public String getErrorCode() {
        return errorCode;
    }
}