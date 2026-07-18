package com.thinklab.infrastructure.adapter.in.web.dto.response;

import com.thinklab.domain.model.TenantAudit;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

/**
 * Response DTO: Public-facing projection of a forensic audit log entry for Organizations.
 * This record represents a single immutable point-in-time event recorded within the
 * Administrative Center. It follows the Projection Pattern to ensure Domain isolation
 * while providing high-fidelity traceability metadata to API consumers.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 *     <li><b>Immutable Egress:</b> Guarantees data stability during JSON serialization.</li>
 *     <li><b>Forensic Context:</b> Includes transaction correlation and execution metadata.</li>
 *     <li><b>AOT Optimized:</b> Compiled reflection-free serialization via Micronaut Serde.</li>
 * </ul>
 *
 * @param id         Internal unique audit entry identifier.
 * @param txId       Correlation identifier grouping related hierarchical operations.
 * @param tenantId   The system identifier of the audited organization.
 * @param operation  The business action performed (e.g., ROOT_PROVISIONING, SUSPENSION).
 * @param status     The final outcome of the operation (e.g., SUCCESS, FAILURE).
 * @param executorId The identity of the administrative agent who authorized the action.
 * @param timestamp  The UTC instant when the event was permanently recorded.
 * @param metadata   Rich contextual metadata map containing mutation details.
 */
@Serdeable
@Introspected
@Schema(
        name = "TenantAuditResponse",
        description = "Standardized forensic metadata representing a specific lifecycle or administrative event of an organization."
)
public record TenantAuditResponse(
        @NonNull @Schema(description = "Unique audit ID", example = "51df1192-ab14-4fa4-875b-d008361b0d32")
        String id,

        @NonNull @Schema(description = "Transaction correlation ID", example = "f46561f1-3e64-40a0-bedb-88b414ce34ae")
        String txId,

        @NonNull @Schema(description = "Target Organization ID", example = "79e83370-d699-4d28-817f-acc401490ed5")
        String tenantId,

        @NonNull @Schema(description = "Business operation type", example = "ROOT_PROVISIONING")
        String operation,

        @NonNull @Schema(description = "Operation resulting status", example = "SUCCESS")
        String status,

        @NonNull @Schema(description = "Identity of the authorizing agent", example = "staff-engineer-01")
        String executorId,

        @NonNull @Schema(description = "UTC timestamp of the event")
        Instant timestamp,

        @NonNull @Schema(description = "Contextual operation metadata")
        Map<String, Object> metadata
) {

    /**
     * Factory: Projects a Domain Aggregate into this Infrastructure Response DTO.
     *
     * @param domain The immutable TenantAudit domain model.
     * @return A sanitized TenantAuditResponse ready for API delivery.
     */
    public static TenantAuditResponse fromDomain(@NonNull TenantAudit domain) {
        return new TenantAuditResponse(
                domain.id(),
                domain.txId(),
                domain.tenantId(),
                domain.operation(),
                domain.status(),
                domain.executorId(),
                domain.timestamp(),
                Map.copyOf(domain.metadata())
        );
    }
}