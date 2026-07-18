package com.thinklab.infrastructure.adapter.in.web.dto.request;

import com.thinklab.application.usecase.command.UpdateTenantProfileCommand;
import com.thinklab.domain.valueobject.TenantProfile;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Infrastructure DTO: Represents the HTTP request payload for updating an
 * Organization's (Tenant) metadata profile.
 * This record acts as the web boundary for mutations involving legal identification,
 * localization, and contact data. It enforces fail-fast validation before
 * reaching the Application Layer.
 *
 * <p><b>Architectural Principles:</b></p>
 * <ul>
 *     <li><b>Deep Validation:</b> Ensures nested profile invariants are respected.</li>
 *     <li><b>Immutability:</b> Java Record ensures data integrity during the reactive pipeline.</li>
 *     <li><b>Mapping Isolation:</b> Encapsulates the transition to the Application Command.</li>
 * </ul>
 *
 * @param profile  The comprehensive legal and localization metadata to be updated.
 * @param executor Identification of the administrative agent authorizing the change.
 */
@Serdeable
@Introspected
@Schema(
        name = "UpdateTenantProfileRequest",
        description = "Payload required to modify organizational metadata (Tax, Location, Contact)."
)
public record UpdateTenantProfileRequest(
        @Valid
        @NotNull(message = "Tenant profile metadata is mandatory")
        @Schema(description = "Updated tax, localization, and contact metadata block")
        TenantProfile profile,

        @NotBlank(message = "Executor identification is mandatory")
        @Size(max = 100, message = "Executor identification exceeds the safety limit of 100 characters")
        @Schema(description = "Identification of the agent executing the update", example = "admin-compliance-01")
        String executor
) {

    /**
     * Factory Method: Translates this Web DTO into a pure Application Command.
     * This mapping ensures that the Application layer remains strictly decoupled
     * from HTTP transport specifics.
     *
     * @param tenantId The system identifier retrieved from the URI Path.
     * @return A sanitized {@link UpdateTenantProfileCommand} instance.
     */
    public UpdateTenantProfileCommand toCommand(String tenantId) {
        return new UpdateTenantProfileCommand(
                tenantId,
                this.executor,
                this.profile
        );
    }
}