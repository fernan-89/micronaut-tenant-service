package com.thinklab.application.port.in;

import com.thinklab.application.usecase.query.ListTenantsQuery;
import com.thinklab.domain.model.Tenant;
import jakarta.annotation.Nonnull;
import reactor.core.publisher.Flux;

/**
 * Application Port: Input boundary for listing and filtering Organizations (Tenants).
 * Following the CQRS principle for pure read-side operations, this interface is
 * engineered for high-performance reactive streaming of hierarchical organizational
 * structures within a Global SaaS ecosystem.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Non-blocking Stream:</b> Utilizes {@link Flux} to ensure zero-latency data egress and support backpressure.</li>
 * <li><b>Hierarchy-Aware:</b> Specifically designed to navigate between Root Holdings and their Branch sub-tenants.</li>
 * <li><b>Defensive Boundary:</b> Enforces input sanitization via validated Query objects before reaching the domain.</li>
 * <li><b>Reactive Sovereignty:</b> Fully integrated into the Project Reactor pipeline for maximum scalability.</li>
 * </ul>
 *
 * @version 1.0.0
 */
public interface ListTenantsUseCase {

    /**
     * Executes the paginated and filtered retrieval of organizations.
     * <p>This method orchestrates the discovery of Root Tenants or specific sub-tenant
     * clusters depending on the hierarchical context provided in the query.</p>
     *TenantFullResponse
     * @param query The {@link ListTenantsQuery} encapsulating hierarchy filters and pagination metadata.
     * @return A {@link Flux} streaming the found {@link Tenant} aggregates in a non-blocking fashion.
     * @throws NullPointerException if the provided query is null, preserving pipeline integrity (Fail-Fast).
     */
    @Nonnull
    Flux<Tenant> execute(@Nonnull ListTenantsQuery query);
}