package com.thinklab.application.interactor;

import com.thinklab.application.port.in.GetTenantAuditLogsUseCase;
import com.thinklab.application.port.out.TenantAuditRepositoryPort;
import com.thinklab.domain.model.TenantAudit;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.Objects;

/**
 * Application Interactor: Implementation of the {@link GetTenantAuditLogsUseCase} input port.
 * This service is responsible for retrieving the immutable forensic audit trail of an Organization.
 *
 * <p><b>Architectural Principles:</b></p>
 * <ul>
 *     <li><b>Immutability:</b> Data retrieved here is strictly for auditing and cannot be altered.</li>
 *     <li><b>Non-blocking:</b> Fully reactive pipeline using Project Reactor (Flux).</li>
 * </ul>
 */
@Slf4j
@Singleton
public class GetTenantAuditLogsInteractor implements GetTenantAuditLogsUseCase {

    private final TenantAuditRepositoryPort auditRepository;

    /**
     * Explicit constructor injection to ensure compatibility with Micronaut's Dependency Injection
     * and avoid proxy generation issues (NoSuchMethodError).
     *
     * @param auditRepository The outbound port for audit data access.
     */
    @Inject
    public GetTenantAuditLogsInteractor(TenantAuditRepositoryPort auditRepository) {
        this.auditRepository = auditRepository;
    }

    /**
     * Retrieves the chronological forensic audit trail for a given tenant.
     *
     * @param tenantId The unique identifier of the tenant.
     * @return A {@link Flux} emitting the sequence of {@link TenantAudit} events.
     */
    @Nonnull
    @Override
    public Flux<TenantAudit> execute(@Nonnull String tenantId) {
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");

        return auditRepository.findByTenantId(tenantId) // Ou o nome do método que você usa no repositório
                .doOnSubscribe(s -> log.debug("[ACTION: GET_AUDIT_LOGS] [ID: {}] - Retrieving forensic history.", tenantId))
                .doOnError(e -> log.error("[ACTION: GET_AUDIT_LOGS] [ID: {}] - Failed to retrieve audit logs: {}",
                        tenantId, e.getMessage()));
    }
}