package com.thinklab.infrastructure.adapter.in.web.dto.request;

import com.thinklab.application.usecase.command.CreateTenantCommand;
import com.thinklab.domain.valueobject.TenantProfile;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable; // <--- ADICIONE ESTA LINHA
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Infrastructure DTO: Represents the HTTP request payload for provisioning a new Organization (Tenant).
 * This object acts as the primary entry boundary, enforcing strict syntactic validation
 * and providing metadata for automated OpenAPI documentation.
 *
 * <p><b>Architectural Principles:</b></p>
 * <ul>
 *     <li><b>Protocol Translation:</b> Shields the application core from HTTP-specific structures.</li>
 *     <li><b>Defensive Boundary:</b> Enforces fail-fast validation using Jakarta Bean Validation.</li>
 *     <li><b>AOT Ready:</b> Optimized for Micronaut's reflection-free serialization.</li>
 * </ul>
 *
 * @param name     Official legal or trade name of the organization.
 * @param parentId Optional ID of the parent organization (for Branch units).
 * @param profile  Global localization and tax identification metadata.
 * @param executor Identification of the agent authorizing the provisioning.
 */
@Serdeable
@Introspected
@Schema(
        name = "CreateTenantRequest",
        description = "Payload required to initiate the provisioning of a new Holding or Branch organization."
)
public record CreateTenantRequest(
        @NotBlank(message = "Organization name is mandatory")
        @Size(min = 2, max = 150, message = "Name must be between 2 and 150 characters")
        @Schema(description = "Legal name of the organization", example = "Novelis Global Corp")
        String name,

        @Nullable
        @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Parent ID contains invalid characters")
        @Schema(description = "Optional parent identifier for branch units", example = "THINKLAB-ROOT-HQ")
        String parentId,

        @Valid
        @NotNull(message = "Tenant profile metadata is mandatory")
        @Schema(description = "Tax, localization, and contact metadata")
        TenantProfile profile,

        @NotBlank(message = "Executor identification is mandatory")
        @Schema(description = "Identification of the administrative agent", example = "provisioning-system-01")
        String executor
) {

    /**
     * Factory Method: Translates this Web DTO into a pure Application Command.
     * This mapping ensures that the Application layer remains strictly decoupled
     * from the Web/JSON infrastructure.
     *
     * @return A sanitized {@link CreateTenantCommand} instance.
     */
    public CreateTenantCommand toCommand() {
        return new CreateTenantCommand(
                this.name,
                this.parentId,
                this.profile,
                this.executor
        );
    }
}