package com.thinklab.application.port.in;

import com.thinklab.application.usecase.query.GetTenantQuery;
import com.thinklab.domain.model.Tenant;
import jakarta.annotation.Nonnull;
import reactor.core.publisher.Mono;

/**
 * Application Port: Input boundary for retrieving a specific Organization (Tenant).
 * Following the CQRS principle, this interface represents a pure read-side operation
 * engineered for high-performance state retrieval within a Global SaaS ecosystem.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Read-Only Intent:</b> Specifically designed for fast lookup of the current aggregate state without audit overhead.</li>
 * <li><b>Non-blocking Execution:</b> Utilizes {@link Mono} to ensure zero-latency integration with reactive persistence.</li>
 * <li><b>Domain Isolation:</b> Decouples retrieval criteria from infrastructure specifics via standardized Query objects.</li>
 * <li><b>Resilient Error Signaling:</b> Defines clear contractual obligations for handling missing resources.</li>
 * </ul>
 *
 * @version 1.0.0
 */
public interface GetTenantUseCase {

    /**
     * Executes the retrieval of a single Tenant by its unique identifier.
     * <p>This method is optimized for operational paths requiring immediate
     * access to organizational metadata and lifecycle status.</p>
     *
     * @param query The {@link GetTenantQuery} encapsulating the target unique identifier.
     * @return A {@link Mono} emitting the found {@link Tenant} aggregate.
     * @throws NullPointerException if the provided query is null, preserving pipeline integrity (Fail-Fast).
     * @apiNote Emits a {@code BusinessException} (e.g., TENANT_NOT_FOUND) through the reactive stream if
     *          the identifier does not exist in the registry.
     */
    @Nonnull
    Mono<Tenant> execute(@Nonnull GetTenantQuery query);
}