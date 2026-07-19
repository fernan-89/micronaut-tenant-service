package com.thinklab.application.interactor;

import com.thinklab.application.port.in.ReactivateTenantUseCase;
import com.thinklab.application.port.out.TenantAuditRepositoryPort;
import com.thinklab.application.port.out.TenantRepositoryPort;
import com.thinklab.application.usecase.command.ReactivateTenantCommand;
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
 * Application Interactor: Implementation of the {@link ReactivateTenantUseCase} input port.
 * This service orchestrates the process of restoring a SUSPENDED organization to its
 * fully operational ACTIVE status, ensuring all state invariants and forensic audit
 * requirements are strictly met for Global SaaS compliance.
 *
 * <p><b>Architectural Principles:</b></p>
 * <ul>
 *     <li><b>Non-blocking:</b> Fully integrated with Project Reactor for high-throughput orchestration.</li>
 *     <li><b>Defensive:</b> Enforces fail-fast patterns for non-existent organizations.</li>
 *     <li><b>Traceable:</b> Every restoration is immutably linked to the authorizing agent.</li>
 * </ul>
 *
 * @author Thinklab Engineering
 * @version 1.0.0
 */
@Slf4j
@Singleton
public class ReactivateTenantInteractor implements ReactivateTenantUseCase {

    private final TenantRepositoryPort tenantRepository;
    private final TenantAuditRepositoryPort auditRepository;

    /**
     * Explicit constructor injection to ensure compatibility with Micronaut's Dependency Injection
     * and avoid proxy generation issues (NoSuchMethodError) previously caused by Lombok.
     */
    @Inject
    public ReactivateTenantInteractor(
            TenantRepositoryPort tenantRepository,
            TenantAuditRepositoryPort auditRepository) {
        this.tenantRepository = tenantRepository;
        this.auditRepository = auditRepository;
    }

    /**
     * Executes the reactivation workflow for an organization.
     *
     * @param command The validated intent to restore tenant operations.
     * @return A {@link Mono} emitting the updated {@link Tenant} aggregate.
     * @throws BusinessException if the organization is not found or the state transition is illegal.
     */
    @Nonnull
    @Override
    public Mono<Tenant> execute(@Nonnull ReactivateTenantCommand command) {
        Objects.requireNonNull(command, "Reactivation command cannot be null");

        log.info("[ACTION: REACTIVATE_TENANT] - Initiating restoration for Tenant [{}] authorized by [{}]",
                command.tenantId(), command.executor());

        return tenantRepository.findById(command.tenantId())
                .switchIfEmpty(Mono.error(new BusinessException("TENANT_NOT_FOUND",
                        "The requested organization does not exist in the global registry.")))
                .map(tenant -> tenant.reactivate(command.executor()))
                .flatMap(tenantRepository::update)
                .flatMap(reactivatedTenant -> registerForensicAudit(reactivatedTenant, command.executor())
                        .thenReturn(reactivatedTenant))
                .doOnSuccess(tenant -> log.info("[ACTION: REACTIVATE_TENANT] - Tenant [{}] successfully restored to ACTIVE status.",
                        tenant.id()))
                .doOnError(e -> {
                    if (!(e instanceof BusinessException)) {
                        log.error("[ACTION: REACTIVATE_TENANT] - Critical failure during reactivation pipeline for Tenant [{}]: {}",
                                command.tenantId(), e.getMessage());
                    }
                });
    }

    /**
     * Internal dispatcher to persist hierarchical audit data.
     */
    private Mono<TenantAudit> registerForensicAudit(Tenant tenant, String executor) {
        // Conversão explícita de UUID para String para compatibilidade com o formato de auditoria
        String tenantIdStr = tenant.id().toString();
        String parentIdStr = tenant.parentId() != null ? tenant.parentId().toString() : null;

        TenantAudit auditEntry = tenant.isBranch()
                ? TenantAudit.createBranchAudit(tenantIdStr, parentIdStr, "TENANT_REACTIVATION", executor, null)
                : TenantAudit.createRootAudit(tenantIdStr, "TENANT_REACTIVATION", executor, null);

        return auditRepository.save(auditEntry);
    }
}