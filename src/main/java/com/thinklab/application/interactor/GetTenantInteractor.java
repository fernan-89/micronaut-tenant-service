package com.thinklab.application.interactor;

import com.thinklab.application.port.in.GetTenantUseCase;
import com.thinklab.application.port.out.TenantRepositoryPort;
import com.thinklab.application.usecase.query.GetTenantQuery;
import com.thinklab.domain.exception.TenantNotFoundException;
import com.thinklab.domain.model.Tenant;
import jakarta.annotation.Nonnull;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Application Interactor: Implementation of the {@link GetTenantUseCase} input port.
 * This service provides read-only access to the Organization registry, following
 * the CQRS principle by strictly separating retrieval logic from state mutations.
 *
 * <p>It enforces boundary validation and ensures that data retrieval failures
 * are emitted as semantic domain exceptions, allowing infrastructure adapters
 * to project them as standardized error responses (RFC 7807).</p>
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 *     <li><b>CQRS Segregation:</b> Pure retrieval operation with no side-effects.</li>
 *     <li><b>Reactive Sovereignty:</b> Fully non-blocking non-interruptible data pipeline.</li>
 *     <li><b>Resilient Error Handling:</b> Semantic mapping of missing resources (404 logic).</li>
 * </ul>
 */
@Slf4j
@Singleton
@RequiredArgsConstructor
public class GetTenantInteractor implements GetTenantUseCase {

    private final TenantRepositoryPort tenantRepository;

    /**
     * Executes the retrieval pipeline for a specific Organization.
     *
     * @param query The validated query containing the unique target identifier.
     * @return A {@link Mono} emitting the found {@link Tenant} aggregate.
     * @throws NullPointerException if the provided query is null (Fail-Fast).
     * @apiNote Emits a {@link TenantNotFoundException} if the identifier does not exist in persistence.
     */
    @Override
    @Nonnull
    public Mono<Tenant> execute(@Nonnull GetTenantQuery query) {
        Objects.requireNonNull(query, "GetTenantQuery cannot be null.");

        return tenantRepository.findById(query.tenantId())
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("[ACTION: GET_TENANT] [ID: {}] - Orchestration halted: Organization not found.", query.tenantId());
                    return Mono.error(new TenantNotFoundException(query.tenantId()));
                }))
                .doOnSubscribe(s -> log.info("[ACTION: GET_TENANT] [ID: {}] - Initiating state retrieval pipeline.", query.tenantId()))
                .doOnSuccess(t -> log.info("[ACTION: GET_TENANT] [ID: {}] - Entity state successfully retrieved and projected.", t.id()))
                .doOnError(error -> {
                    if (!(error instanceof TenantNotFoundException)) {
                        log.error("[ACTION: GET_TENANT] [ID: {}] - Critical pipeline failure: {}",
                                query.tenantId(), error.getMessage());
                    }
                });
    }
}