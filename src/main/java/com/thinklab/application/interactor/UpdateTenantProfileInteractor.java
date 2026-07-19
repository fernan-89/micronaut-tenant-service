package com.thinklab.application.interactor;

import com.thinklab.application.port.in.UpdateTenantProfileUseCase;
import com.thinklab.application.port.out.TenantAuditRepositoryPort;
import com.thinklab.application.port.out.TenantRepositoryPort;
import com.thinklab.application.usecase.command.UpdateTenantProfileCommand;
import com.thinklab.domain.exception.BusinessException;
import com.thinklab.domain.model.Tenant;
import com.thinklab.domain.model.TenantAudit;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

/**
 * Application Interactor: Implementation of the {@link UpdateTenantProfileUseCase} input port.
 */
@Slf4j
@Singleton
public class UpdateTenantProfileInteractor implements UpdateTenantProfileUseCase {

    private final TenantRepositoryPort tenantRepository;
    private final TenantAuditRepositoryPort auditRepository;

    @Inject
    public UpdateTenantProfileInteractor(TenantRepositoryPort tenantRepository, TenantAuditRepositoryPort auditRepository) {
        this.tenantRepository = tenantRepository;
        this.auditRepository = auditRepository;
    }

    @Nonnull
    @Override
    public Mono<Tenant> execute(@Nonnull UpdateTenantProfileCommand command) {
        Objects.requireNonNull(command, "Update command cannot be null");

        // Conversão segura do ID do comando para UUID antes de buscar
        UUID tenantUuid = UUID.fromString(command.tenantId());

        log.info("[ACTION: UPDATE_TENANT_PROFILE] - Initiating metadata update for Tenant [{}] authorized by [{}]",
                command.tenantId(), command.executor());

        return tenantRepository.findById(command.tenantId()) // Certifique-se que o port aceita String ou mude para tenantUuid
                .switchIfEmpty(Mono.error(new BusinessException("TENANT_NOT_FOUND",
                        "The target organization was not found in the global registry.")))
                .map(tenant -> tenant.updateProfile(command.profile(), command.executor()))
                .flatMap(tenantRepository::update)
                .flatMap(updatedTenant -> registerForensicAudit(updatedTenant, command.executor())
                        .thenReturn(updatedTenant))
                .doOnSuccess(tenant -> {
                    if (tenant != null) {
                        log.info("[ACTION: UPDATE_TENANT_PROFILE] - Tenant [{}] successfully updated and audited.", tenant.id());
                    }
                })
                .doOnError(e -> {
                    if (!(e instanceof BusinessException)) {
                        log.error("[ACTION: UPDATE_TENANT_PROFILE] - Critical system failure during profile update for Tenant [{}]: {}",
                                command.tenantId(), e.getMessage());
                    }
                });
    }

    private Mono<TenantAudit> registerForensicAudit(Tenant tenant, String executor) {
        // Conversão explícita para String para compatibilidade com a Auditoria
        String tenantIdStr = tenant.id().toString();
        String parentIdStr = tenant.parentId() != null ? tenant.parentId().toString() : null;

        TenantAudit auditEntry = tenant.isBranch()
                ? TenantAudit.createBranchAudit(tenantIdStr, parentIdStr, "PROFILE_UPDATE", executor, null)
                : TenantAudit.createRootAudit(tenantIdStr, "PROFILE_UPDATE", executor, null);

        return auditRepository.save(auditEntry);
    }
}