package com.thinklab.infrastructure.adapter.out.mongo;

import com.thinklab.application.port.out.TenantAuditRepositoryPort;
import com.thinklab.domain.model.TenantAudit;
import com.thinklab.infrastructure.adapter.out.mongo.entity.TenantAuditEntity;
import com.thinklab.infrastructure.adapter.out.mongo.repository.TenantAuditMongoRepository;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject; // Importante
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Persistence Adapter: Implementation of the {@link TenantAuditRepositoryPort} for MongoDB.
 */
@Slf4j
@Singleton
public class TenantAuditRepositoryAdapter implements TenantAuditRepositoryPort {

    private final TenantAuditMongoRepository repository;

    // Construtor explícito com @Inject resolve o erro de BeanInstantiationException
    @Inject
    public TenantAuditRepositoryAdapter(TenantAuditMongoRepository repository) {
        this.repository = repository;
    }

    @Nonnull
    @Override
    public Mono<TenantAudit> save(@Nonnull TenantAudit audit) {
        Objects.requireNonNull(audit, "Audit aggregate cannot be null");
        log.debug("[ACTION: PERSIST_AUDIT] - Initiating storage for Tenant [{}] Operation [{}]",
                audit.tenantId(), audit.operation());

        return repository.save(toEntity(audit))
                .map(this::toDomain)
                .doOnError(e -> log.error("[ACTION: PERSIST_AUDIT] - Critical failure persisting audit trail for Tenant [{}]: {}",
                        audit.tenantId(), e.getMessage()));
    }

    @Override
    @Nonnull
    public Mono<TenantAudit> findById(@Nonnull String id) {
        return repository.findById(id)
                .map(this::toDomain)
                .doOnError(e -> log.error("[ACTION: FIND_AUDIT] - Error finding audit by ID [{}]: {}", id, e.getMessage()));
    }

    @Nonnull
    @Override
    public Flux<TenantAudit> findByTxId(@Nonnull String txId) {
        Objects.requireNonNull(txId, "Transaction correlation ID cannot be null");
        return repository.findByTxId(txId)
                .map(this::toDomain);
    }

    @Nonnull
    @Override
    public Flux<TenantAudit> findByTenantId(@Nonnull String tenantId) {
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        log.debug("[ACTION: FIND_AUDIT_BY_TENANT] - Retrieving audit trail for Tenant [{}]", tenantId);

        return repository.findByTenantId(tenantId)
                .map(this::toDomain)
                .doOnError(e -> log.error("[ACTION: FIND_AUDIT_BY_TENANT] - Error finding audit by Tenant ID [{}]: {}", tenantId, e.getMessage()));
    }

    private TenantAuditEntity toEntity(TenantAudit domain) {
        return new TenantAuditEntity(
                domain.id(),
                domain.txId(),
                domain.tenantId(),
                domain.operation(),
                domain.status(),
                domain.executorId(),
                domain.timestamp(),
                domain.metadata()
        );
    }

    private TenantAudit toDomain(@Nonnull TenantAuditEntity entity) {
        return new TenantAudit(
                entity.id(),
                entity.txId(),
                entity.tenantId(),
                entity.operation(),
                entity.status(),
                entity.executorId(),
                entity.timestamp(),
                entity.metadata()
        );
    }
}