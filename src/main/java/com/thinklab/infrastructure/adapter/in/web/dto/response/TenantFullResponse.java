package com.thinklab.infrastructure.adapter.in.web.dto.response;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Response DTO: Aggregate view of a Tenant and its complete lifecycle audit trail.
 * This record consolidates the current state projection of a {@link TenantResponse}
 * with its associated {@link TenantAuditResponse} historical events.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 *     <li><b>Unified Egress:</b> Reduces API round-trips for clients requiring a full forensic view.</li>
 *     <li><b>AOT Optimized:</b> Reflection-free serialization via Micronaut Serde.</li>
 *     <li><b>Domain Isolation:</b> Shield's core business entities from public API requirements.</li>
 * </ul>
 *
 * @param tenant    The current state projection of the requested Organization (Holding or Branch).
 * @param auditLogs An ordered list (forensic timeline) of all lifecycle events associated with this tenant.
 */
@Serdeable
@Introspected
@Schema(
        name = "TenantFullResponse",
        description = "Aggregated projection containing current organizational state and its associated forensic audit trail."
)
public record TenantFullResponse(
        @NonNull
        @Schema(description = "The current administrative metadata for the requested organization")
        TenantResponse tenant,

        @NonNull
        @Schema(description = "The complete immutable list of forensic audit entries recording organizational mutations")
        List<TenantAuditResponse> auditLogs
) {
    /**
     * Factory: Orchestrates the construction of the consolidated response view.
     * Ensures null-safety for the audit timeline to maintain API contract consistency.
     */
    public static TenantFullResponse of(
            @NonNull TenantResponse tenant,
            @NonNull List<TenantAuditResponse> auditLogs
    ) {
        return new TenantFullResponse(
                tenant,
                auditLogs != null ? List.copyOf(auditLogs) : List.of()
        );
    }
}