package com.thinklab.domain.exception;

import jakarta.annotation.Nonnull;

/**
 * Domain Business Exception thrown when a requested Organization (Tenant) cannot
 * be located within the persistence layer.
 * This specific exception extends {@link BusinessException} to enforce the
 * standardized "TENANT_NOT_FOUND" error code, ensuring that infrastructure
 * adapters (like the GlobalExceptionHandler) can invariably translate this
 * into a compliant HTTP 404 (Not Found) response.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 *     <li><b>Standardized Categorization:</b> Guarantees consistent error categorization for clients.</li>
 *     <li><b>Semantic Constructors:</b> Encourages precise error reporting over generic strings.</li>
 *     <li><b>Null-Safety Fallbacks:</b> Provides safe default messages if parameters are corrupted.</li>
 *     <li><b>Domain Isolation:</b> Strictly decoupled from HTTP-specific constructs.</li>
 * </ul>
 */
public class TenantNotFoundException extends BusinessException {

    private static final String ERROR_CODE = "TENANT_NOT_FOUND";
    private static final String DEFAULT_MESSAGE = "The requested organization could not be found.";

    /**
     * Constructs a new TenantNotFoundException for an ID-based search failure.
     *
     * @param tenantId The unique system identifier that was not found.
     */
    public TenantNotFoundException(@Nonnull String tenantId) {
        super(ERROR_CODE, tenantId == null || tenantId.isBlank()
                ? DEFAULT_MESSAGE
                : String.format("Organization with ID [%s] could not be found.", tenantId.trim()));
    }

    /**
     * Constructs a new TenantNotFoundException for a search failure based on specific field criteria.
     * Useful for lookups by TaxID, Business Unit, or Corporate Email.
     *
     * @param field The specific attribute used for the lookup.
     * @param value The value that resulted in no match.
     */
    public TenantNotFoundException(@Nonnull String field, @Nonnull String value) {
        super(ERROR_CODE, (field == null || field.isBlank() || value == null || value.isBlank())
                ? DEFAULT_MESSAGE
                : String.format("Organization with %s [%s] could not be found.", field.trim(), value.trim()));
    }
}