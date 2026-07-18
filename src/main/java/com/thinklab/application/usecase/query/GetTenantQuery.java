package com.thinklab.application.usecase.query;

import io.micronaut.core.annotation.Introspected;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Application Query: Encapsulates the intent to retrieve a specific Organization.
 * This record acts as an input boundary, ensuring that only well-formed identifiers
 * reach the persistence layer, mitigating potential malformed query execution.
 *
 * <p><b>Architectural Principles:</b></p>
 * <ul>
 *     <li><b>Syntactic Shield:</b> Prevents NoSQL/BSON injection via regex pattern matching.</li>
 *     <li><b>Sanitization:</b> Normalizes input strings to ensure cache and index hits.</li>
 *     <li><b>AOT Ready:</b> Optimized for Micronaut's reflection-free metadata generation.</li>
 * </ul>
 *
 * @param tenantId The unique system identifier (UUID) of the target Organization.
 */
@Slf4j
@Introspected
public record GetTenantQuery(
        @NotBlank(message = "Tenant ID is mandatory for retrieval")
        @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Tenant ID contains invalid characters")
        String tenantId
) {

    /**
     * Compact constructor for defensive programming and input sanitization.
     */
    public GetTenantQuery {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");

        // Input Sanitization
        tenantId = tenantId.trim();

        if (tenantId.isBlank()) {
            log.error("[ACTION: GET_TENANT_QUERY_VALIDATION] - Validation failed: Tenant ID is blank.");
        }
    }
}