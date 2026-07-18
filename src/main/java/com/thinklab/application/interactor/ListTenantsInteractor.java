package com.thinklab.application.interactor;

import com.thinklab.application.port.in.ListTenantsUseCase;
import com.thinklab.application.port.out.TenantRepositoryPort;
import com.thinklab.application.usecase.query.ListTenantsQuery;
import com.thinklab.domain.model.Tenant;
import io.micronaut.data.model.Pageable;
import jakarta.annotation.Nonnull;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.Objects;

/**
 * Application Interactor: Implementation of the {@link ListTenantsUseCase} input port.
 * This service orchestrates the high-performance retrieval of organizational structures,
 * supporting hierarchical navigation between Root Holdings and Business Units (Branches)
 * through a reactive, non-blocking pipeline.
 *
 * <p><b>Architectural Principles:</b></p>
 * <ul>
 *     <li><b>CQRS Read Side:</b> Pure retrieval operation with zero side-effects.</li>
 *     <li><b>Hierarchy-Aware:</b> Atomically switches lookups based on parent context.</li>
 *     <li><b>Reactive Egress:</b> Leverages {@link Flux} for efficient data streaming and backpressure.</li>
 * </ul>
 *
 * @author Thinklab Engineering
 * @version 1.0.0
 */
@Slf4j
@Singleton
@RequiredArgsConstructor
public class ListTenantsInteractor implements ListTenantsUseCase {

    private final TenantRepositoryPort tenantRepository;

    /**
     * Executes the paginated discovery of organizations.
     *
     * @param query The validated {@link ListTenantsQuery} criteria.
     * @return A {@link Flux} streaming the matching {@link Tenant} aggregates.
     * @throws NullPointerException if the query is null (Fail-Fast).
     */
    @Nonnull
    @Override
    public Flux<Tenant> execute(@Nonnull ListTenantsQuery query) {
        Objects.requireNonNull(query, "ListTenantsQuery is mandatory for retrieval.");

        Pageable pageable = Pageable.from(query.page(), query.size());

        return Flux.defer(() -> {
                    log.info("[ACTION: LIST_TENANTS] [PARENT: {}] [PAGE: {}] [SIZE: {}] - Initiating hierarchical discovery.",
                            query.parentId() != null ? query.parentId() : "ROOT", query.page(), query.size());

                    return query.isRootLookup()
                            ? tenantRepository.findRoots(pageable)
                            : tenantRepository.findByParentId(query.parentId(), pageable);
                })
                .doOnComplete(() -> log.debug("[ACTION: LIST_TENANTS] - Discovery pipeline completed successfully."))
                .doOnError(error -> log.error("[ACTION: LIST_TENANTS] - Critical failure during tenant retrieval: {}",
                        error.getMessage()));
    }
}