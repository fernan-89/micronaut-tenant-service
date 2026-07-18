package com.thinklab.infrastructure.adapter.out.mongo;

import com.thinklab.domain.model.Tenant;
import com.thinklab.application.port.out.TenantRepositoryPort;
import com.thinklab.infrastructure.adapter.out.mongo.entity.TenantEntity;
import com.thinklab.infrastructure.adapter.out.mongo.repository.TenantMongoRepository;
import io.micronaut.data.model.Pageable;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

/**
 * Persistence Adapter: Implementation of the {@link TenantRepositoryPort} for MongoDB.
 * Acts as an Anti-Corruption Layer, performing the translation between Domain types
 * and Database Entities.
 */
@Slf4j
@Singleton
public class TenantRepositoryAdapter implements TenantRepositoryPort {

    private final TenantMongoRepository repository;

    /**
     * Explicit constructor injection to ensure compatibility with Micronaut's Dependency Injection
     * and avoid proxy generation issues (NoSuchMethodError).
     *
     * @param repository The Micronaut Data repository for MongoDB.
     */
    @Inject
    public TenantRepositoryAdapter(TenantMongoRepository repository) {
        this.repository = repository;
    }

    @Nonnull
    @Override
    public Mono<Tenant> save(@Nonnull Tenant tenant) {
        Objects.requireNonNull(tenant, "Tenant aggregate cannot be null.");
        return repository.save(TenantEntity.fromDomain(tenant))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new IllegalStateException("Database failed to return the saved entity. Check if Micronaut Data is treating this insert as an update."))))
                .map(TenantEntity::toDomain)
                .doOnSuccess(t -> log.info("[ACTION: PERSIST_TENANT] - ID: {} provisioned.", t.id()))
                .doOnError(e -> log.error("[ACTION: PERSIST_TENANT] - Failed to persist: {}", e.getMessage()));
    }

    @Nonnull
    @Override
    public Mono<Tenant> update(@Nonnull Tenant tenant) {
        Objects.requireNonNull(tenant, "Tenant aggregate cannot be null.");
        return repository.update(TenantEntity.fromDomain(tenant))
                .map(TenantEntity::toDomain)
                .doOnSuccess(t -> log.debug("[ACTION: UPDATE_TENANT] - ID: {} synchronized.", t.id()))
                .doOnError(e -> log.error("[ACTION: UPDATE_TENANT] - Failed: {}", e.getMessage()));
    }

    @Nonnull
    @Override
    public Mono<Tenant> findById(@Nonnull String id) {
        return Mono.fromCallable(() -> UUID.fromString(id))
                .flatMap(repository::findById)
                .map(TenantEntity::toDomain)
                .doOnError(e -> log.error("[ACTION: FIND_TENANT] - Failure for [{}]: {}", id, e.getMessage()));
    }

    @Nonnull
    @Override
    public Mono<Boolean> existsByTaxId(@Nonnull String taxId) {
        return repository.existsByProfileTaxId(taxId)
                .defaultIfEmpty(false); // Proteção para evitar fluxo vazio no "Exists"
    }

    @Nonnull
    @Override
    public Flux<Tenant> findRoots(@Nonnull Pageable pageable) {
        return repository.findByParentIdIsNull(pageable)
                .map(TenantEntity::toDomain);
    }

    @Nonnull
    @Override
    public Flux<Tenant> findByParentId(@Nonnull String parentId, @Nonnull Pageable pageable) {
        // Conversão explícita de String para UUID
        return repository.findByParentId(UUID.fromString(parentId), pageable)
                .map(TenantEntity::toDomain);
    }
}