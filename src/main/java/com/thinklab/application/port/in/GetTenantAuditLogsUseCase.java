package com.thinklab.application.port.in;

import com.thinklab.domain.model.TenantAudit;
import jakarta.annotation.Nonnull;
import reactor.core.publisher.Flux;

/**
 * Application Port: Input boundary for retrieving the forensic audit trail of an Organization.
 * This interface defines the contract for streaming immutable event logs associated
 * with a specific Tenant. It is engineered to support high-fidelity historical state
 * reconstruction and compliance reporting within a multi-tenant ecosystem.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 *     <li><b>Stream-Oriented Egress:</b> Utilizes {@link Flux} to provide non-blocking, backpressure-aware delivery of audit records.</li>
 *     <li><b>Forensic Integrity:</b> Ensures chronological access to the immutable ledger of organizational mutations.</li>
 *     <li><b>Infrastructure Decoupling:</b> Shields the application from specific log storage implementations (e.g., MongoDB, ELK, or Cold Storage).</li>
 *     <li><b>Reactive Sovereignty:</b> Guarantees zero-latency integration with high-throughput reporting pipelines.</li>
 * </ul>
 *
 * @author Thinklab Engineering
 * @version 1.0.0
 */
public interface GetTenantAuditLogsUseCase {

    /**
     * Executes the retrieval pipeline to extract all forensic logs for a target organization.
     * <p>The implementation must ensure that the resulting stream respects multi-tenant
     * isolation and provides a high-fidelity representation of administrative actions.</p>
     *
     * @param tenantId The unique system identifier of the organization whose audit trail is requested.
     * @return A {@link Flux} streaming the found {@link TenantAudit} records in chronological order.
     * @throws NullPointerException if the provided tenantId is null, preserving pipeline integrity (Fail-Fast).
     * @apiNote Returns an empty {@link Flux} if the tenant exists but has no recorded events,
     *          satisfying the expected behavior for clean-slate organizations.
     */
    @Nonnull
    Flux<TenantAudit> execute(@Nonnull String tenantId);
}