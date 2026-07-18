package com.thinklab.application.interactor;

import com.thinklab.application.port.in.SuspendTenantUseCase;
import com.thinklab.application.port.out.TenantAuditRepositoryPort;
import com.thinklab.application.port.out.TenantRepositoryPort;
import com.thinklab.application.usecase.command.SuspendTenantCommand;
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
 * Application Interactor: Implementation of the {@link SuspendTenantUseCase} input port.
 * This service orchestrates the administrative process of temporarily halting an
 * organization's operations.
 */
@Slf4j
@Singleton
public class SuspendTenantInteractor implements SuspendTenantUseCase {

    private final TenantRepositoryPort tenantRepository;
    private final TenantAuditRepositoryPort auditRepository;

    /**
     * Explicit constructor injection to ensure compatibility with Micronaut's DI.
     */
    @Inject
    public SuspendTenantInteractor(TenantRepositoryPort tenantRepository, TenantAuditRepositoryPort auditRepository) {
        this.tenantRepository = tenantRepository;
        this.auditRepository = auditRepository;
    }

    /**
     * Executes the tenant suspension business flow.
     *
     * @param command The validated intent to suspend an organization.
     * @return A {@link Mono} emitting the updated and persisted {@link Tenant}.
     */
    @Nonnull
    @Override
    public Mono<Tenant> execute(@Nonnull SuspendTenantCommand command) {
        Objects.requireNonNull(command, "Suspension command cannot be null");

        log.info("[ACTION: SUSPEND_TENANT] - Initiating suspension for Tenant [{}] authorized by [{}]",
                command.tenantId(), command.executor());

        return tenantRepository.findById(command.tenantId())
                .switchIfEmpty(Mono.error(new BusinessException("TENANT_NOT_FOUND",
                        "The requested organization does not exist in the global registry.")))
                .map(tenant -> tenant.suspend(command.executor()))
                .flatMap(tenantRepository::save)
                .flatMap(suspendedTenant -> registerForensicAudit(suspendedTenant, command.executor())
                        .thenReturn(suspendedTenant))
                .doOnSuccess(tenant -> {
                    if (tenant != null) {
                        log.info("[ACTION: SUSPEND_TENANT] - Tenant [{}] successfully transitioned to SUSPENDED state.", tenant.id());
                    }
                })
                .doOnError(e -> {
                    if (!(e instanceof BusinessException)) {
                        log.error("[ACTION: SUSPEND_TENANT] - Critical failure during suspension pipeline for Tenant [{}]: {}",
                                command.tenantId(), e.getMessage());
                    }
                });
    }

    /**
     * Audit Dispatcher: Persists the hierarchical audit record based on tenant role.
     */
    private Mono<TenantAudit> registerForensicAudit(Tenant tenant, String executor) {
        // Conversão explícita de UUID para String para manter compatibilidade com TenantAudit
        String tenantIdStr = tenant.id().toString();
        String parentIdStr = tenant.parentId() != null ? tenant.parentId().toString() : null;

        TenantAudit auditEntry = tenant.isBranch()
                ? TenantAudit.createBranchAudit(tenantIdStr, parentIdStr, "TENANT_SUSPENSION", executor, null)
                : TenantAudit.createRootAudit(tenantIdStr, "TENANT_SUSPENSION", executor, null);

        return auditRepository.save(auditEntry);
    }
}