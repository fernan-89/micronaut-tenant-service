package com.thinklab.application.port.in;

import com.thinklab.domain.model.Tenant;
import com.thinklab.domain.model.TenantAudit;
import jakarta.annotation.Nonnull;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Application Port: Input boundary for retrieving a comprehensive 360-degree view of a Tenant.
 * Following the CQRS principle for read-side operations, this use case orchestrates the
 * simultaneous retrieval of an Organization's current state and its complete immutable
 * forensic audit trail.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 *     <li><b>Non-blocking Composition:</b> Engineered to consolidate multiple data sources (Aggregates and Audits) reactively.</li>
 *     <li><b>Forensic Completeness:</b> Ensures that the business identity is always presented alongside its historical lifecycle events.</li>
 *     <li><b>Defensive Signaling:</b> Strictly enforces the propagation of {@code TenantNotFoundException} if the identifier is invalid.</li>
 * </ul>
 *
 * @author Thinklab Engineering
 * @version 1.0.0
 */
public interface GetTenantFullViewUseCase {

    /**
     * Data structure representing the consolidated view of a Tenant and its history.
     * Modeled as a local record to maintain immutability during application-to-web projection.
     */
    record TenantFullView(
            @Nonnull Tenant tenant,
            @Nonnull List<TenantAudit> auditLogs
    ) {}

    /**
     * Executes the high-fidelity retrieval and composition of the Tenant Full View.
     *
     * @param tenantId The unique system identifier (UUID) of the target organization.
     * @return A {@link Mono} emitting the {@link TenantFullView} containing the aggregate and its audit logs.
     * @throws NullPointerException if the provided tenantId is null.
     * @apiNote Emits a {@code TenantNotFoundException} signal through the reactive pipeline if the record is missing.
     */
    @Nonnull
    Mono<TenantFullView> execute(@Nonnull String tenantId);
}