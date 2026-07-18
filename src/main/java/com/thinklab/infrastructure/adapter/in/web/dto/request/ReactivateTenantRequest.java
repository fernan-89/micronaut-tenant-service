package com.thinklab.infrastructure.adapter.in.web.dto.request;

import com.thinklab.application.usecase.command.ReactivateTenantCommand;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Infrastructure DTO: Represents the HTTP request payload for restoring an
 * organization's (Tenant) status to ACTIVE.
 * This record acts as the system's boundary for operational restoration,
 * enforcing strict validation on executor identity and business justification
 * to maintain the integrity of the permanent forensic audit trail.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 *     <li><b>Forensic Accountability:</b> Mandates a business reason for state restoration.</li>
 *     <li><b>Edge Defense:</b> Prevents malformed or excessive payloads at the entry point.</li>
 *     <li><b>AOT Optimized:</b> Reflection-free serialization for high-performance scale.</li>
 * </ul>
 *
 * @param executor Identification of the administrative agent authorizing the reactivation.
 * @param reason   The mandatory business justification for restoring operations (Tier 3 Compliance).
 */
@Serdeable
@Introspected
@Schema(
        name = "ReactivateTenantRequest",
        description = "Payload required to restore a suspended organization to active status. Business reason is mandatory."
)
public record ReactivateTenantRequest(
        @NotBlank(message = "Executor identification is mandatory")
        @Size(max = 100, message = "Executor identification exceeds the safety limit of 100 characters")
        @Schema(description = "Identification of the agent executing the action", example = "admin-compliance-unit-01")
        String executor,

        @NotBlank(message = "Reactivation reason is mandatory for forensic compliance")
        @Size(min = 10, max = 500, message = "Justification must be between 10 and 500 characters")
        @Schema(description = "Formal business justification for the status restoration", example = "Compliance verification completed successfully.")
        String reason
) {

    /**
     * Factory Method: Translates this Web DTO into a pure Application Command.
     * Encapsulates the mapping logic to shield the Application Layer from HTTP specifics.
     *
     * @param tenantId The unique system identifier retrieved from the URI Path.
     * @return A sanitized {@link ReactivateTenantCommand} instance.
     */
    public ReactivateTenantCommand toCommand(String tenantId) {
        return new ReactivateTenantCommand(
                tenantId,
                this.executor,
                this.reason
        );
    }
}