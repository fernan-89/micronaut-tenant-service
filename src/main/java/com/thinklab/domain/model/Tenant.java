package com.thinklab.domain.model;

import com.thinklab.domain.exception.BusinessException;
import com.thinklab.domain.valueobject.TenantProfile;
import com.thinklab.domain.valueobject.TenantStatus;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root: Core domain model representing an Organization (Tenant or Sub-tenant).
 * Engineered for global-scale SaaS with support for enterprise hierarchies (Holding/Branch).
 */
public record Tenant(
        @Nonnull UUID id,
        @Nullable UUID parentId,
        @Nonnull String name,
        @Nonnull TenantStatus status,
        @Nonnull TenantProfile profile,
        @Nonnull String createdBy,
        @Nonnull Instant createdAt,
        @Nullable String updatedBy,
        @Nullable Instant updatedAt,
        @Nonnull Long version
) {

    public Tenant {
        Objects.requireNonNull(id, "Organization ID is mandatory");
        Objects.requireNonNull(name, "Organization name is mandatory");
        Objects.requireNonNull(status, "Operational status is mandatory");
        Objects.requireNonNull(profile, "Organization profile (KYC/Localization) is mandatory");
        Objects.requireNonNull(createdBy, "Provisioning agent is mandatory");
        Objects.requireNonNull(createdAt, "Creation timestamp is mandatory");
        Objects.requireNonNull(version, "Concurrency version is mandatory");

        if (name.isBlank()) {
            throw new BusinessException("INVALID_TENANT_DATA", "Organization name cannot be blank.");
        }
    }

    public static Tenant provision(@Nonnull UUID id, @Nullable UUID parentId, @Nonnull String name, @Nonnull TenantProfile profile, @Nonnull String executor) {
        return new Tenant(id, parentId, name.trim(), TenantStatus.ACTIVE, profile, executor, Instant.now(), null, null, 0L);
    }

    public static Tenant createRoot(@Nonnull String name, @Nonnull TenantProfile profile, @Nonnull String executor) {
        return create(name, null, profile, executor);
    }

    public static Tenant createBranch(@Nonnull String name, @Nonnull UUID parentId, @Nonnull TenantProfile profile, @Nonnull String executor) {
        Objects.requireNonNull(parentId, "Branch must be linked to a Parent ID");
        return create(name, parentId, profile, executor);
    }

    private static Tenant create(String name, UUID parentId, TenantProfile profile, String executor) {
        // Agora geramos o UUID diretamente como objeto, sem o .toString()
        return new Tenant(UUID.randomUUID(), parentId, name.trim(), TenantStatus.ACTIVE, profile, executor, Instant.now(), null, null, 0L);
    }

    public Tenant suspend(@Nonnull String executor) {
        this.status.validateTransitionTo(TenantStatus.SUSPENDED);
        return new Tenant(id, parentId, name, TenantStatus.SUSPENDED, profile, createdBy, createdAt, executor, Instant.now(), version);
    }

    public Tenant reactivate(@Nonnull String executor) {
        this.status.validateTransitionTo(TenantStatus.ACTIVE);
        return new Tenant(id, parentId, name, TenantStatus.ACTIVE, profile, createdBy, createdAt, executor, Instant.now(), version);
    }

    public Tenant revoke(@Nonnull String executor) {
        this.status.validateTransitionTo(TenantStatus.REVOKED);
        return new Tenant(id, parentId, name, TenantStatus.REVOKED, profile, createdBy, createdAt, executor, Instant.now(), version);
    }

    public Tenant updateProfile(@Nonnull TenantProfile newProfile, @Nonnull String executor) {
        Objects.requireNonNull(newProfile, "New profile cannot be null");
        return new Tenant(id, parentId, name, status, newProfile, createdBy, createdAt, executor, Instant.now(), version);
    }

    public boolean isHolding() { return parentId == null; }
    public boolean isBranch() { return parentId != null; }
}