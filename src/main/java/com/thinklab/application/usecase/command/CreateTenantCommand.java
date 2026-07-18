package com.thinklab.application.usecase.command;

import com.thinklab.domain.valueobject.TenantProfile;
import io.micronaut.core.annotation.Introspected;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Application Command: Encapsulates the intent to provision a new Organization (Tenant).
 * This record acts as the primary entry boundary, enforcing strict input validation
 * and defensive sanitization to ensure that the organizational identity and its
 * global profile are consistent before reaching the domain logic.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 *     <li><b>Hierarchy-Aware:</b> Supports the creation of both Root Holdings and Branch units.</li>
 *     <li><b>Immutable State:</b> Java Record guarantees thread-safety in reactive pipelines.</li>
 *     <li><b>Edge Observability:</b> Integrated logging for failed validation tracking.</li>
 * </ul>
 *
 * @param name     Official legal or trade name of the organization.
 * @param parentId Optional ID of the parent organization. If null, a ROOT (Holding) is provisioned.
 * @param profile  Validated legal, tax, and localization metadata (Value Object).
 * @param executor The administrative agent or system authorizing the provisioning.
 */
@Slf4j
@Introspected
public record CreateTenantCommand(
        @NotBlank(message = "Organization name is mandatory")
        String name,

        @Nullable
        @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Parent ID contains invalid characters")
        String parentId,

        @Valid
        @NotNull(message = "Tenant profile metadata is mandatory")
        TenantProfile profile,

        @NotBlank(message = "Executor identification is mandatory")
        String executor
) {

    /**
     * Compact constructor for defensive programming and input sanitization.
     * Acts as the final gatekeeper for data integrity at the application boundary.
     */
    public CreateTenantCommand {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(profile, "profile cannot be null");
        Objects.requireNonNull(executor, "executor cannot be null");

        // Input Sanitization
        name = name.trim();
        executor = executor.trim();

        if (parentId != null) {
            parentId = parentId.trim();
            if (parentId.isBlank()) parentId = null;
        }

        if (name.isBlank() || executor.isBlank()) {
            log.error("[ACTION: CREATE_TENANT_VALIDATION] - Edge validation failed: Empty mandatory fields detected.");
        }
    }

    /**
     * Predicate to determine if this command targets a root organization (Holding).
     */
    public boolean isRootProvisioning() {
        return parentId == null;
    }
}