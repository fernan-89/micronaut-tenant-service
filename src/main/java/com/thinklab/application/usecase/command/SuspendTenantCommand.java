package com.thinklab.application.usecase.command;

import io.micronaut.core.annotation.Introspected;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Application Command: Encapsulates the intent to suspend an active Organization (Tenant).
 * This record enforces strict input validation and defensive sanitization at the system
 * boundary, ensuring that all suspension requests are authorized and carry
 * a mandatory business justification for forensic auditing.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 *     <li><b>Immutability:</b> Java Record ensures thread-safety in reactive pipelines.</li>
 *     <li><b>Forensic Integrity:</b> Mandatory capture of executor and reason for compliance.</li>
 *     <li><b>Edge Observability:</b> Integrated logging for failed validation attempts.</li>
 * </ul>
 *
 * @param tenantId The unique system identifier (UUID) of the target organization.
 * @param executor The administrative agent or system authorizing the suspension.
 * @param reason   The business justification for halting operations (Max 500 chars).
 */
@Slf4j
@Introspected
public record SuspendTenantCommand(
        @NotBlank(message = "Tenant ID is mandatory")
        @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Tenant ID contains invalid characters")
        String tenantId,

        @NotBlank(message = "Executor identification is mandatory")
        String executor,

        @NotBlank(message = "Suspension reason is mandatory")
        @Size(max = 500, message = "Justification text is too long")
        String reason
) {

    /**
     * Compact constructor for defensive programming and input sanitization.
     * Acts as the final gatekeeper for data integrity before reaching the Interactor.
     */
    public SuspendTenantCommand {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(executor, "executor cannot be null");
        Objects.requireNonNull(reason, "reason cannot be null");

        // Sanitization
        tenantId = tenantId.trim();
        executor = executor.trim();
        reason = reason.trim();

        if (tenantId.isBlank() || executor.isBlank() || reason.isBlank()) {
            log.error("[ACTION: SUSPEND_TENANT_VALIDATION] - Edge validation failed: Empty fields detected for Tenant [{}]", tenantId);
        }
    }
}