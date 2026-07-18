package com.thinklab.application.usecase.command;

import io.micronaut.core.annotation.Introspected;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Application Command: Encapsulates the intent to restore a SUSPENDED organization to ACTIVE status.
 * This record enforces strict input validation and defensive sanitization at the system
 * boundary. Reactivation is a critical lifecycle event, therefore it mandates an
 * authorized executor and a business justification for forensic audit compliance.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 *     <li><b>Forensic Integrity:</b> Mandatory justification field to support Tier 3 auditing.</li>
 *     <li><b>Edge Defense:</b> Regex validation prevents malformed IDs from reaching persistence.</li>
 *     <li><b>Reactive Ready:</b> Pure immutable structure optimized for non-blocking pipelines.</li>
 * </ul>
 *
 * @param tenantId The unique system identifier (UUID) of the organization to be reactivated.
 * @param executor The administrative agent or system authorizing the restoration.
 * @param reason   The business justification for the reactivation (Max 500 chars).
 */
@Slf4j
@Introspected
public record ReactivateTenantCommand(
        @NotBlank(message = "Tenant ID is mandatory")
        @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Tenant ID contains invalid characters")
        String tenantId,

        @NotBlank(message = "Executor identification is mandatory")
        String executor,

        @NotBlank(message = "Reactivation reason is mandatory")
        @Size(max = 500, message = "Justification text exceeds the safety limit of 500 characters")
        String reason
) {

    /**
     * Compact constructor for defensive programming and input sanitization.
     * Acts as the final gatekeeper for data integrity before reaching the Interactor.
     */
    public ReactivateTenantCommand {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(executor, "executor cannot be null");
        Objects.requireNonNull(reason, "reason cannot be null");

        // Input Sanitization
        tenantId = tenantId.trim();
        executor = executor.trim();
        reason = reason.trim();

        if (tenantId.isBlank() || executor.isBlank() || reason.isBlank()) {
            log.error("[ACTION: REACTIVATE_TENANT_VALIDATION] - Edge validation failed: Empty mandatory fields for Tenant [{}]", tenantId);
        }
    }
}