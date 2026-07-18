package com.thinklab.application.usecase.query;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Application Query: Encapsulates the criteria for listing organizations with hierarchy awareness.
 * Engineered for Global SaaS, this record supports filtering by parent organization (Holding)
 * to retrieve specific branch clusters, alongside standard pagination.
 *
 * <p><b>Architectural Roles:</b></p>
 * <ul>
 *     <li><b>Hierarchy Filter:</b> Optional {@code parentId} allows navigation between Holdings and Branches.</li>
 *     <li><b>Resource Protection:</b> Enforces strict limits on page size to prevent DoS via large egress.</li>
 *     <li><b>Input Sanitization:</b> Compact constructor ensures clean IDs for database queries.</li>
 * </ul>
 *
 * @param parentId Optional ID of the parent organization. If null, retrieves root organizations (Holdings).
 * @param page     Zero-based page index.
 * @param size     Number of records per page (Max 100).
 */
@Slf4j
@Introspected
public record ListTenantsQuery(
        @Nullable
        @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Parent ID contains invalid characters")
        String parentId,

        @Min(0)
        Integer page,

        @Max(100)
        Integer size
) {

    /**
     * Compact constructor for sanitization and default value enforcement.
     * Acts as the final gatekeeper before the query reaches the Interactor.
     */
    public ListTenantsQuery {
        // Default values for pagination if not provided
        page = (page == null) ? 0 : page;
        size = (size == null) ? 20 : size;

        // Sanitization of identifiers
        if (parentId != null) {
            parentId = parentId.trim();
            if (parentId.isBlank()) {
                log.warn("[ACTION: LIST_TENANTS_VALIDATION] - Empty parentId provided, treating as ROOT lookup.");
                parentId = null;
            }
        }
    }

    /**
     * Helper to determine if we are looking for Root organizations or specific branches.
     */
    public boolean isRootLookup() {
        return parentId == null;
    }
}