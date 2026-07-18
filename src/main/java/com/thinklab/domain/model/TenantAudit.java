package com.thinklab.domain.model;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain Model: Immutable aggregate representing a forensic audit entry.
 * Specially engineered to maintain strict bindings between Root Tenants and Sub-tenants.
 *
 * <p><b>Mission-Critical Binding Principles:</b></p>
 * <ul>
 *     <li><b>Traceable Hierarchy:</b> For branches, parentId is mandatorily captured in metadata.</li>
 *     <li><b>Atomic Integrity:</b> Validates that every sub-tenant mutation points back to its root context.</li>
 *     <li><b>Append-Only:</b> State is strictly immutable, satisfying Tier 3 global compliance.</li>
 * </ul>
 */
public record TenantAudit(
        @Nonnull String id,
        @Nonnull String txId,
        @Nonnull String tenantId,
        @Nonnull String operation,
        @Nonnull String status,
        @Nonnull String executorId,
        @Nonnull Instant timestamp,
        @Nonnull Map<String, Object> metadata
) {

    public TenantAudit {
        Objects.requireNonNull(id, "Audit ID is mandatory");
        Objects.requireNonNull(txId, "Transaction ID is mandatory");
        Objects.requireNonNull(tenantId, "Target Tenant ID is mandatory");
        Objects.requireNonNull(operation, "Operation type is mandatory");
        Objects.requireNonNull(status, "Status is mandatory");
        Objects.requireNonNull(executorId, "Executor is mandatory");
        Objects.requireNonNull(timestamp, "Timestamp is mandatory");

        metadata = metadata != null ? Map.copyOf(metadata) : Collections.emptyMap();
    }

    /**
     * Factory: Provisions a forensic record for a Sub-tenant (Branch) operation.
     * Ensures the hierarchy binding is explicitly preserved in the audit metadata.
     */
    @Nonnull
    public static TenantAudit createBranchAudit(
            @Nonnull String branchId,
            @Nonnull String parentId,
            @Nonnull String operation,
            @Nonnull String executorId,
            @Nullable Map<String, Object> additionalContext
    ) {
        Map<String, Object> bindingMetadata = new HashMap<>(additionalContext != null ? additionalContext : Map.of());
        bindingMetadata.put("hierarchy_role", "BRANCH");
        bindingMetadata.put("parent_id_binding", parentId); // O "Nó" da amarra

        return new TenantAudit(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(), // Contextual TxId should ideally come from MDC
                branchId,
                operation,
                "SUCCESS",
                executorId,
                Instant.now(),
                bindingMetadata
        );
    }

    /**
     * Factory: Standard success audit for Root Organizations.
     */
    @Nonnull
    public static TenantAudit createRootAudit(
            @Nonnull String tenantId,
            @Nonnull String operation,
            @Nonnull String executorId,
            @Nullable Map<String, Object> context
    ) {
        return new TenantAudit(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                tenantId,
                operation,
                "SUCCESS",
                executorId,
                Instant.now(),
                context
        );
    }
}