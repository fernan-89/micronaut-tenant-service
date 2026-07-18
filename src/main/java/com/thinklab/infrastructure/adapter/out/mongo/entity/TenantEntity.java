package com.thinklab.infrastructure.adapter.out.mongo.entity;

import com.thinklab.domain.model.Tenant;
import com.thinklab.domain.valueobject.TenantProfile;
import com.thinklab.domain.valueobject.TenantStatus;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.Index;
import io.micronaut.data.annotation.Indexes;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Version;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.time.Instant;
import java.util.UUID;

@Serdeable
@Introspected
@MappedEntity("tenants")
@Indexes({
        @Index(columns = {"profile.taxId"}, unique = true),
        @Index(columns = {"parentId"}),
        @Index(columns = {"status"})
})
public record TenantEntity(
        @Id
        @Nonnull
        UUID id,

        @Nullable
        UUID parentId,

        @Nonnull
        String name,

        @Nonnull
        TenantStatus status,

        @Nonnull
        TenantProfile profile,

        @Nonnull
        String createdBy,

        @Nonnull
        Instant createdAt,

        @Nullable
        String updatedBy,

        @Nullable
        Instant updatedAt,

        @Version
        @Nonnull
        Long version
) {

    public static TenantEntity fromDomain(@Nonnull Tenant domain) {
        return new TenantEntity(
                domain.id(),      // Já é UUID
                domain.parentId(), // Já é UUID
                domain.name(),
                domain.status(),
                domain.profile(),
                domain.createdBy(),
                domain.createdAt(),
                domain.updatedBy(),
                domain.updatedAt(),
                domain.version()
        );
    }

    public Tenant toDomain() {
        return new Tenant(
                this.id,          // Já é UUID
                this.parentId,    // Já é UUID
                this.name,
                this.status,
                this.profile,
                this.createdBy,
                this.createdAt,
                this.updatedBy,
                this.updatedAt,
                this.version
        );
    }
}