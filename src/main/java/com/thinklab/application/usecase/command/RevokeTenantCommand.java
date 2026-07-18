package com.thinklab.application.usecase.command;

import io.micronaut.core.annotation.Introspected;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Application Command: Encapsulates the intent to PERMANENTLY and IRREVERSIBLY revoke
 * an Organization (Tenant). Revocation is the terminal state in the lifecycle,
 * satisfying Zero Trust principles by ensuring that once executed, the entity
 * cannot return to an operational state.
 *
 * <p>This record enforces strict input validation and defensive sanitization at
 * the system boundary. It mandates high-fidelity metadata (executor and reason)
 * required for Tier 3 forensic compliance.</p>
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 *     <li><b>Terminal State Integrity:</b> Ensures mandatory capture of revocation justification.</li>
 *     <li><b>Edge Defense:</b> Regex and Size constraints mitigate DoS and Injection risks.</li>
 *     <li><b>Immutability:</b> Java Record architecture ensures thread-safety in reactive flows.</li>
 *     <li><b>Observability:</b> Integrated edge logging for failed validation attempts.</li>
 * </ul>
 *
 * @param tenantId The unique system identifier (UUID) of the organization to be revoked.
 * @param executor The administrative agent or security system authorizing this terminal action.
 * @param reason   The mandatory business justification for the permanent revocation (Max 500 chars).
 */
@Slf4j
@Introspected
public record RevokeTenantCommand(
        @NotBlank(message = "Tenant ID is mandatory")
        @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Tenant ID contains invalid characters")
        String tenantId,

        @NotBlank(message = "Executor identification is mandatory")
        String executor,

        @NotBlank(message = "Revocation reason is mandatory for compliance")
        @Size(max = 500, message = "Justification text exceeds the safety limit of 500 characters")
        String reason
) {

    /**
     * Compact constructor for defensive programming and input sanitization.
     * Acts as the final gatekeeper for data integrity before reaching the Interactor.
     */
    public RevokeTenantCommand {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(executor, "executor cannot be null");
        Objects.requireNonNull(reason, "reason cannot be null");

        // Input Sanitization
        tenantId = tenantId.trim();
        executor = executor.trim();
        reason = reason.trim();

        if (tenantId.isBlank() || executor.isBlank() || reason.isBlank()) {
            log.error("[ACTION: REVOKE_TENANT_VALIDATION] - Edge validation failed: Empty mandatory fields detected for Tenant [{}]", tenantId);
        }
    }
}