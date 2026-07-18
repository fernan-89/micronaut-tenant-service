package com.thinklab.application.interactor;

import com.thinklab.application.port.in.RevokeTenantUseCase;
import com.thinklab.application.port.out.TenantAuditRepositoryPort;
import com.thinklab.application.port.out.TenantRepositoryPort;
import com.thinklab.application.usecase.command.RevokeTenantCommand;
import com.thinklab.domain.exception.BusinessException;
import com.thinklab.domain.model.Tenant;
import com.thinklab.domain.model.TenantAudit;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;

/**
 * Application Interactor: Implementation of the {@link RevokeTenantUseCase} input port.
 * This service orchestrates the permanent and irreversible revocation of an Organization.
 */
@Slf4j
@Singleton
public class RevokeTenantInteractor implements RevokeTenantUseCase {

    private final TenantRepositoryPort tenantRepository;
    private final TenantAuditRepositoryPort auditRepository;

    @Inject
    public RevokeTenantInteractor(TenantRepositoryPort tenantRepository, TenantAuditRepositoryPort auditRepository) {
        this.tenantRepository = tenantRepository;
        this.auditRepository = auditRepository;
    }

    @Override
    @Nonnull
    public Mono<Tenant> execute(@Nonnull RevokeTenantCommand command) {
        Objects.requireNonNull(command, "RevokeTenantCommand cannot be null.");

        return tenantRepository.findById(command.tenantId())
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("[ACTION: REVOKE_TENANT] [ID: {}] - Orchestration halted: Organization not found.", command.tenantId());
                    return Mono.error(new BusinessException("TENANT_NOT_FOUND", "The targeted organization does not exist."));
                }))
                .map(existingTenant -> existingTenant.revoke(command.executor()))
                .flatMap(tenantRepository::update)
                .flatMap(revokedTenant -> createAuditLog(revokedTenant, command.executor(), command.reason())
                        .thenReturn(revokedTenant))
                .doOnSubscribe(s -> log.warn("[ACTION: REVOKE_TENANT] [ID: {}] [EXECUTOR: {}] - CRITICAL: Initiating terminal revocation sequence.",
                        command.tenantId(), command.executor()))
                .doOnSuccess(t -> {
                    if (t != null) {
                        log.warn("[ACTION: REVOKE_TENANT] [ID: {}] - CRITICAL: Organization successfully revoked.", t.id());
                    }
                })
                .doOnError(error -> {
                    if (!(error instanceof BusinessException)) {
                        log.error("[ACTION: REVOKE_TENANT] [ID: {}] - CRITICAL: Terminal sequence failure: {}",
                                command.tenantId(), error.getMessage());
                    }
                });
    }

    private Mono<TenantAudit> createAuditLog(Tenant tenant, String executor, String reason) {
        // AQUI ESTAVA O ERRO: Adicionado .toString() abaixo
        return auditRepository.save(TenantAudit.createRootAudit(
                tenant.id().toString(),
                "TENANT_REVOCATION",
                executor,
                Map.of(
                        "reason", reason,
                        "terminalAction", "TRUE",
                        "finalStatus", tenant.status().name(),
                        "traceabilityTier", "3"
                )
        ));
    }
}