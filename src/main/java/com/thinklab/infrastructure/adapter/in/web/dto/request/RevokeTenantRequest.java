package com.thinklab.infrastructure.adapter.in.web.dto.request;

import com.thinklab.application.usecase.command.RevokeTenantCommand;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Infrastructure DTO: Represents the HTTP request payload for permanently revoking
 * an Organization (Tenant).
 * This object serves as the terminal entry boundary, enforcing strict validation
 * on business justification and executor identity to satisfy Tier 3 forensic
 * compliance and Zero Trust security principles.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 *     <li><b>Terminal Integrity:</b> Mandates high-fidelity metadata for irreversible actions.</li>
 *     <li><b>Defensive Boundary:</b> Syntactic validation prevents malformed execution at the edge.</li>
 *     <li><b>AOT Optimized:</b> Designed for reflection-free serialization in high-throughput environments.</li>
 *     <li><b>Protocol Isolation:</b> Decouples URI Path identifiers from request body semantics.</li>
 * </ul>
 *
 * @param executor Identification of the administrative agent authorizing the terminal revocation.
 * @param reason   The mandatory business justification for the permanent decommissioning (Min 10 chars).
 */
@Serdeable
@Introspected
@Schema(
        name = "RevokeTenantRequest",
        description = "Payload required to permanently and irreversibly revoke an organization. Mandates compliance metadata."
)
public record RevokeTenantRequest(
        @NotBlank(message = "Executor identification is mandatory")
        @Size(max = 100, message = "Executor identification exceeds the safety limit of 100 characters")
        @Schema(description = "Identification of the agent executing the terminal action", example = "security-admin-alpha-01")
        String executor,

        @NotBlank(message = "Revocation reason is mandatory for forensic compliance")
        @Size(min = 10, max = 500, message = "Justification must be between 10 and 500 characters")
        @Schema(description = "Formal business justification for the irreversible revocation", example = "Security compromise detected: immediate decommissioning required.")
        String reason
) {

    /**
     * Factory Method: Translates this Web DTO into a terminal Application Command.
     * Encapsulates the mapping logic to preserve strict architectural layer isolation.
     *
     * @param tenantId The unique system identifier retrieved from the URI Path.
     * @return A sanitized {@link RevokeTenantCommand} instance ready for orchestration.
     */
    public RevokeTenantCommand toCommand(String tenantId) {
        return new RevokeTenantCommand(
                tenantId,
                this.executor,
                this.reason
        );
    }
}