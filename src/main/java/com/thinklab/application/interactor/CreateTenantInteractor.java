package com.thinklab.application.interactor;

import com.thinklab.application.port.in.CreateTenantUseCase;
import com.thinklab.application.port.out.TenantAuditRepositoryPort;
import com.thinklab.application.port.out.TenantRepositoryPort;
import com.thinklab.application.usecase.command.CreateTenantCommand;
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
 * Application Interactor: Implementation of the {@link CreateTenantUseCase} input port.
 * This service orchestrates the complex workflow of provisioning a new Organization (Tenant).
 * It enforces business uniqueness via TaxID, generates deterministic identifiers for
 * idempotency, and guarantees a permanent forensic audit trail.
 *
 * <p><b>Architectural Principles:</b></p>
 * <ul>
 *     <li><b>Atomic Provisioning:</b> Ensures state and audit are synchronized.</li>
 *     <li><b>Non-blocking:</b> Fully reactive pipeline using Project Reactor.</li>
 *     <li><b>Global Scale:</b> Engineered for hierarchical Holdings and Branches.</li>
 * </ul>
 */
@Slf4j
@Singleton
public class CreateTenantInteractor implements CreateTenantUseCase {

    private final TenantRepositoryPort tenantRepository;
    private final TenantAuditRepositoryPort auditRepository;

    /**
     * Explicit constructor injection to ensure compatibility with Micronaut's Dependency Injection
     * and avoid proxy generation issues (NoSuchMethodError) previously caused by Lombok.
     */
    @Inject
    public CreateTenantInteractor(
            TenantRepositoryPort tenantRepository,
            TenantAuditRepositoryPort auditRepository) {
        this.tenantRepository = tenantRepository;
        this.auditRepository = auditRepository;
    }

    /**
     * Executes the organization provisioning workflow.
     *
     * @param command The validated intent to create a tenant.
     * @return A {@link Mono} emitting the successfully provisioned {@link Tenant}.
     * @throws BusinessException if the TaxID already exists or domain invariants are violated.
     */
    @Nonnull
    @Override
    public Mono<Tenant> execute(@Nonnull CreateTenantCommand command) {
        Objects.requireNonNull(command, "CreateTenantCommand cannot be null");

        String taxId = command.profile().taxId();

        return tenantRepository.existsByTaxId(taxId)
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("[ACTION: CREATE_TENANT] [TAX_ID: {}] - Provisioning halted: Organization already registered.", taxId);
                        return Mono.error(new BusinessException("TENANT_DUPLICATE",
                                "An organization with this Tax Identifier is already registered in the global registry."));
                    }
                    return provisionNewTenant(command);
                })
                .doOnSubscribe(s -> log.info("[ACTION: CREATE_TENANT] [NAME: {}] - Initiating provisioning orchestration.", command.name()))
                .doOnSuccess(tenant -> {
                    // Proteção restaurada para evitar o NullPointerException do Reactor
                    if (tenant != null) {
                        log.info("[ACTION: CREATE_TENANT] [ID: {}] - Organization successfully provisioned and audited.", tenant.id());
                    } else {
                        log.warn("[ACTION: CREATE_TENANT] - Flow completed empty. Provisioning was skipped or aborted silently.");
                    }
                })
                .doOnError(e -> {
                    if (!(e instanceof BusinessException)) {
                        log.error("[ACTION: CREATE_TENANT] - Critical system failure during provisioning for [{}]: {}",
                                command.name(), e.getMessage());
                    }
                });
    }

    /**
     * Internal logic to instantiate the aggregate and bind persistence with audit.
     */
    private Mono<Tenant> provisionNewTenant(CreateTenantCommand command) {
        // Agora gera o UUID real sem converter para String
        UUID deterministicId = UUID.nameUUIDFromBytes(
                (command.profile().taxId() + command.profile().countryCode()).getBytes()
        );

        // Converte o parentId (String) que vem da web para UUID (se existir)
        UUID parentUuid = null;
        if (command.parentId() != null && !command.parentId().isBlank()) {
            parentUuid = UUID.fromString(command.parentId());
        }

        Tenant newTenant = Tenant.provision(
                deterministicId,
                parentUuid,
                command.name(),
                command.profile(),
                command.executor()
        );

        return tenantRepository.save(newTenant)
                .flatMap(savedTenant -> registerForensicAudit(savedTenant, command.executor())
                        .thenReturn(savedTenant));
    }

    /**
     * Records the creation event in the immutable forensic log.
     */
    private Mono<TenantAudit> registerForensicAudit(Tenant tenant, String executor) {
        // Temporariamente convertido para String para não quebrar a sua classe TenantAudit atual
        String tenantIdStr = tenant.id().toString();
        String parentIdStr = tenant.parentId() != null ? tenant.parentId().toString() : null;

        TenantAudit auditEntry = tenant.isBranch()
                ? TenantAudit.createBranchAudit(tenantIdStr, parentIdStr, "ROOT_PROVISIONING", executor, null)
                : TenantAudit.createRootAudit(tenantIdStr, "ROOT_PROVISIONING", executor, null);

        return auditRepository.save(auditEntry);
    }
}