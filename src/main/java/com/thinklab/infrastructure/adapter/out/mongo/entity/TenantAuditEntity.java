package com.thinklab.infrastructure.adapter.out.mongo.entity;

import com.thinklab.domain.model.TenantAudit;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.Index;
import io.micronaut.data.annotation.Indexes;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;

import java.time.Instant;
import java.util.Map;

/**
 * Infrastructure Entity: Persistence model for the forensic audit trail of Organizations.
 * This record maps directly to the MongoDB "tenant_audits" collection and follows
 * the Append-Only principle to ensure the integrity of administrative history.
 *
 * <p><b>Persistence Strategy:</b></p>
 * <ul>
 *     <li><b>Immutable:</b> Designed strictly for insertion; no update logic is permitted.</li>
 *     <li><b>Indexing:</b> High-performance lookups for transaction correlation and multi-tenant reporting.</li>
 *     <li><b>AOT Optimized:</b> Compiled serialization via Micronaut Serde for low-latency writes.</li>
 * </ul>
 */
@Serdeable
@Introspected
@MappedEntity("tenant_audits")
@Indexes({
        @Index(columns = {"txId"}),
        @Index(columns = {"tenantId"}),
        @Index(columns = {"executorId"}),
        @Index(columns = {"timestamp"})
})
public record TenantAuditEntity(
        @Id
        @NonNull
        String id,

        @NonNull
        String txId,

        @NonNull
        String tenantId,

        @NonNull
        String operation,

        @NonNull
        String status,

        @NonNull
        String executorId,

        @NonNull
        Instant timestamp,

        @NonNull
        Map<String, Object> metadata
) {

    /**
     * Maps a pure Domain Aggregate into this Infrastructure Persistence Entity.
     *
     * @param domain The immutable audit record from the domain layer.
     * @return A mapped TenantAuditEntity ready for MongoDB storage.
     */
    public static TenantAuditEntity fromDomain(@NonNull TenantAudit domain) {
        return new TenantAuditEntity(
                domain.id(),
                domain.txId(),
                domain.tenantId(),
                domain.operation(),
                domain.status(),
                domain.executorId(),
                domain.timestamp(),
                domain.metadata()
        );
    }

    /**
     * Reconstructs the Domain Aggregate from the persistence state.
     *
     * @return A pure TenantAudit domain record.
     */
    public TenantAudit toDomain() {
        return new TenantAudit(
                this.id,
                this.txId,
                this.tenantId,
                this.operation,
                this.status,
                this.executorId,
                this.timestamp,
                this.metadata
        );
    }
}