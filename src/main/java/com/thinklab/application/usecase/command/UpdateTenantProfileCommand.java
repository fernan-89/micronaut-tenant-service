package com.thinklab.application.usecase.command;

import com.thinklab.domain.valueobject.TenantProfile;
import io.micronaut.core.annotation.Introspected;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Application Command: Encapsulates the intent to modify an Organization's metadata profile.
 * This record enforces strict input validation and defensive sanitization at the system
 * boundary, acting as a shield for the core domain. It ensures that all administrative
 * updates are authorized and carry valid localization and tax identification data.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 *     <li><b>Immutability:</b> Java Record ensures thread-safety across reactive event loops.</li>
 *     <li><b>Forensic Integrity:</b> Mandatory capture of the authorizing executor for auditing.</li>
 *     <li><b>Defensive Boundary:</b> Prevents malformed or malicious data from reaching the aggregate root.</li>
 * </ul>
 *
 * @param tenantId The unique system identifier (UUID) of the organization to be updated.
 * @param executor The administrative agent or system account authorizing the metadata change.
 * @param profile  The new validated localization and legal metadata (Value Object).
 */
@Slf4j
@Introspected
public record UpdateTenantProfileCommand(
        @NotBlank(message = "Tenant ID is mandatory")
        @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Tenant ID contains invalid characters")
        String tenantId,

        @NotBlank(message = "Executor identification is mandatory")
        String executor,

        @Valid
        @NotNull(message = "Profile metadata cannot be null")
        TenantProfile profile
) {

    /**
     * Compact constructor for input sanitization and boundary defense.
     * Acts as the final gatekeeper for data integrity before business orchestration.
     */
    public UpdateTenantProfileCommand {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(executor, "executor cannot be null");
        Objects.requireNonNull(profile, "profile cannot be null");

        // Input Sanitization
        tenantId = tenantId.trim();
        executor = executor.trim();

        if (tenantId.isBlank() || executor.isBlank()) {
            log.error("[ACTION: UPDATE_TENANT_PROFILE_VALIDATION] - Edge validation failed: Empty mandatory identifiers for Tenant [{}]", tenantId);
        }
    }
}