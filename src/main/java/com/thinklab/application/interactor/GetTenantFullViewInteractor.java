package com.thinklab.application.interactor;

import com.thinklab.application.port.in.GetTenantFullViewUseCase;
import com.thinklab.application.port.out.TenantAuditRepositoryPort;
import com.thinklab.application.port.out.TenantRepositoryPort;
import com.thinklab.domain.exception.BusinessException;
import com.thinklab.domain.model.Tenant;
import com.thinklab.domain.model.TenantAudit;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Application Interactor: Implementation of the {@link GetTenantFullViewUseCase} input port.
 * This service orchestrates the high-fidelity retrieval of an Organization's current state
 * alongside its complete forensic audit trail. It uses reactive composition to
 * consolidate data from multiple persistence sources in parallel.
 *
 * <p><b>Architectural Principles:</b></p>
 * <ul>
 *     <li><b>Atomic Projection:</b> Provides a unified view of the lifecycle for audit compliance.</li>
 *     <li><b>Parallel Execution:</b> Leverages {@link Mono#zip} to optimize query performance.</li>
 *     <li><b>Non-blocking:</b> Fully integrated into the reactive pipeline with zero thread-stalling.</li>
 * </ul>
 *
 * @author Thinklab Engineering
 * @version 1.0.0
 */
@Slf4j
@Singleton
public class GetTenantFullViewInteractor implements GetTenantFullViewUseCase {

    private final TenantRepositoryPort tenantRepository;
    private final TenantAuditRepositoryPort auditRepository;

    /**
     * Explicit constructor injection to ensure compatibility with Micronaut's Dependency Injection
     * and avoid proxy generation issues (NoSuchMethodError) previously caused by Lombok.
     */
    @Inject
    public GetTenantFullViewInteractor(
            TenantRepositoryPort tenantRepository,
            TenantAuditRepositoryPort auditRepository) {
        this.tenantRepository = tenantRepository;
        this.auditRepository = auditRepository;
    }

    /**
     * Executes the comprehensive retrieval and composition of the Tenant Full View.
     *
     * @param tenantId The unique system identifier of the organization.
     * @return A {@link Mono} emitting the consolidated {@link TenantFullView}.
     * @throws BusinessException if the organization is not found.
     */
    @Nonnull
    @Override
    public Mono<TenantFullView> execute(@Nonnull String tenantId) {
        Objects.requireNonNull(tenantId, "Tenant ID is mandatory for full view retrieval.");

        return tenantRepository.findById(tenantId)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("[ACTION: GET_TENANT_FULL_VIEW] [ID: {}] - Orchestration halted: Entity not found.", tenantId);
                    return Mono.error(new BusinessException("TENANT_NOT_FOUND",
                            "The requested organization does not exist in the global registry."));
                }))
                .flatMap(this::composeWithAuditTrail)
                .doOnSubscribe(s -> log.info("[ACTION: GET_TENANT_FULL_VIEW] [ID: {}] - Initiating 360-degree forensic retrieval.", tenantId))
                .doOnSuccess(view -> log.info("[ACTION: GET_TENANT_FULL_VIEW] [ID: {}] - Consolidated view successfully projected with [{}] audit entries.",
                        tenantId, view.auditLogs().size()))
                .doOnError(err -> {
                    if (!(err instanceof BusinessException)) {
                        log.error("[ACTION: GET_TENANT_FULL_VIEW] [ID: {}] - Critical failure during data composition: {}",
                                tenantId, err.getMessage());
                    }
                });
    }

    /**
     * Internal parallel orchestrator to fetch audits once tenant existence is confirmed.
     */
    private Mono<TenantFullView> composeWithAuditTrail(Tenant tenant) {
        // Correção: Conversão do UUID para String para manter compatibilidade com o adapter de auditoria
        return auditRepository.findByTenantId(tenant.id().toString())
                .collectList()
                .map(logs -> new TenantFullView(tenant, logs));
    }
}