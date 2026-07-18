package com.thinklab.infrastructure.adapter.out.mongo.repository;

import com.thinklab.infrastructure.adapter.out.mongo.entity.TenantAuditEntity;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.reactive.ReactorCrudRepository;
import reactor.core.publisher.Flux;

@MongoRepository
public interface TenantAuditMongoRepository extends ReactorCrudRepository<TenantAuditEntity, String> {

    /**
     * Retrieves audit records by transaction ID.
     */
    Flux<TenantAuditEntity> findByTxId(String txId);
    Flux<TenantAuditEntity> findByTenantId(String tenantId);
}