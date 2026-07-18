package com.thinklab.infrastructure.adapter.in.web.dto.request;

import com.thinklab.application.usecase.command.SuspendTenantCommand;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Infrastructure DTO: Represents the HTTP request payload for suspending an Organization (Tenant).
 * This record acts as the entry boundary for operational halts, enforcing the mandatory
 * capture of a business justification and executor identity before reaching the domain logic.
 *
 * <p><b>Architectural Principles:</b></p>
 * <ul>
 *     <li><b>Forensic Accountability:</b> Mandates a reason for all suspension events.</li>
 *     <li><b>Edge Defense:</b> Prevents malformed or empty justifications at the system boundary.</li>
 *     <li><b>Protocol Isolation:</b> Decouples HTTP transport specifics from application commands.</li>
 * </ul>
 *
 * @param executor Identification of the administrative agent authorizing the suspension.
 * @param reason   The mandatory business justification for the service interruption.
 */
@Serdeable
@Introspected
@Schema(
        name = "SuspendTenantRequest",
        description = "Payload required to halt a tenant's operations. Requires explicit business justification."
)
public record SuspendTenantRequest(
        @NotBlank(message = "Executor identification is mandatory")
        @Size(max = 100, message = "Executor identification exceeds the safety limit of 100 characters")
        @Schema(description = "Identification of the agent executing the action", example = "admin-ops-unit-01")
        String executor,

        @NotBlank(message = "Suspension reason is mandatory for forensic compliance")
        @Size(min = 10, max = 500, message = "Justification must be between 10 and 500 characters")
        @Schema(description = "Business justification for the operational halt", example = "Compliance review required due to suspicious activity.")
        String reason
) {

    /**
     * Factory Method: Translates this Web DTO into a pure Application Command.
     * Maps the path identifier and the request body into a sanitized command object.
     *
     * @param tenantId The unique system identifier retrieved from the URI Path.
     * @return A sanitized {@link SuspendTenantCommand} instance.
     */
    public SuspendTenantCommand toCommand(String tenantId) {
        return new SuspendTenantCommand(
                tenantId,
                this.executor,
                this.reason
        );
    }
}