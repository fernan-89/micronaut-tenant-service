package com.thinklab.infrastructure.adapter.out.mongo.repository;

import com.thinklab.infrastructure.adapter.out.mongo.entity.TenantEntity;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.mongodb.annotation.MongoFindQuery;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.reactive.ReactorCrudRepository;
import jakarta.annotation.Nonnull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Micronaut Data Repository: Reactive persistence interface for {@link TenantEntity}.
 * This interface orchestrates high-performance, non-blocking access to the MongoDB
 * "tenants" collection, utilizing AOT compilation for optimal footprint and execution.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 *     <li><b>BSON Optimization:</b> Uses native UUIDs (Subtype 4) for high-efficiency indexing.</li>
 *     <li><b>Reactive Sovereignty:</b> Strictly uses Project Reactor for non-blocking I/O.</li>
 *     <li><b>Deep Query Mapping:</b> Explicitly navigates nested profile metadata.</li>
 * </ul>
 */
@MongoRepository
public interface TenantMongoRepository extends ReactorCrudRepository<TenantEntity, UUID> {

    /**
     * Verifies the existence of an organization by its Tax ID within the nested profile.
     * Essential for global uniqueness enforcement and KYB compliance.
     *
     * @param taxId The legal tax identifier (e.g., CNPJ, VAT) to verify.
     * @return A {@link Mono} emitting true if the tax ID is already registered.
     */
    @MongoFindQuery("{ 'profile.taxId': :taxId }")
    Mono<Boolean> existsByProfileTaxId(@Nonnull String taxId);

    /**
     * Retrieves a paginated stream of root organizations (Holdings).
     * Root organizations are defined by the absence of a parent identifier.
     *
     * @param pageable Pagination and sorting constraints.
     * @return A {@link Flux} streaming the matching Root entities.
     */
    @Nonnull
    Flux<TenantEntity> findByParentIdIsNull(@Nonnull Pageable pageable);

    /**
     * Retrieves a paginated stream of branch organizations (sub-tenants) for a specific parent.
     * This query maintains the hierarchical integrity of Multinational Organizations.
     *
     * @param parentId The unique identifier (UUID) of the parent organization.
     * @param pageable Pagination and sorting metadata.
     * @return A {@link Flux} streaming the found sub-tenant entities.
     */
    @Nonnull
    Flux<TenantEntity> findByParentId(@Nonnull UUID parentId, @Nonnull Pageable pageable);
}