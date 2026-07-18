package com.thinklab.application.port.out;

import com.thinklab.domain.model.Tenant;
import io.micronaut.data.model.Pageable;
import jakarta.annotation.Nonnull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Domain Output Port: Repository contract for the {@link Tenant} aggregate persistence.
 * This interface defines the mandatory persistence behavior for global organizations
 * within the Administrative Center. Following the Hexagonal Architecture, the Core Domain
 * depends solely on this abstraction, ensuring complete isolation from infrastructure
 * frameworks or specific database driver implementations.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 *     <li><b>Non-blocking Execution:</b> Strictly enforces reactive patterns to ensure high-throughput organizational scaling.</li>
 *     <li><b>Data Integrity:</b> Supports optimistic locking and atomic state persistence for high-assurance environments.</li>
 *     <li><b>Hierarchy-Aware:</b> Optimized for traversing complex corporate trees (Holdings and Branches).</li>
 * </ul>
 *
 * @author Thinklab Engineering
 * @version 1.1.0
 */
public interface TenantRepositoryPort {

    /**
     * Persists the initial state of a new organization in the global registry.
     *
     * @param tenant The validated {@link Tenant} aggregate to be persisted.
     * @return A {@link Mono} emitting the successfully persisted {@link Tenant} instance.
     * @throws NullPointerException if the provided aggregate is null.
     */
    @Nonnull
    Mono<Tenant> save(@Nonnull Tenant tenant);

    /**
     * Commits state mutations of an existing organization (e.g., status changes or profile updates).
     *
     * @param tenant The mutated aggregate root carrying the updated state and version.
     * @return A {@link Mono} emitting the updated and synchronized {@link Tenant}.
     */
    @Nonnull
    Mono<Tenant> update(@Nonnull Tenant tenant);

    /**
     * Retrieves a specific organization by its unique system identifier.
     *
     * @param id The system-generated UUID (as String) of the target organization.
     * @return A {@link Mono} emitting the found {@link Tenant}, or an empty signal if non-existent.
     */
    @Nonnull
    Mono<Tenant> findById(@Nonnull String id);

    /**
     * Verifies the global uniqueness of a Tax Identifier (CNPJ/VAT/EIN) in the registry.
     * Essential for KYB compliance and avoiding organizational duplication.
     *
     * @param taxId The legal tax identification number to check.
     * @return A {@link Mono} emitting true if the tax ID is already registered, otherwise false.
     */
    @Nonnull
    Mono<Boolean> existsByTaxId(@Nonnull String taxId);

    /**
     * Retrieves a paginated stream of all root organizations (Holdings) in the system.
     *
     * @param pageable Metadata for pagination and sorting (e.g., page number, size).
     * @return A {@link Flux} streaming the found Root {@link Tenant} aggregates.
     */
    @Nonnull
    Flux<Tenant> findRoots(@Nonnull Pageable pageable);

    /**
     * Retrieves a paginated stream of branch organizations belonging to a specific parent.
     *
     * @param parentId The unique identifier (as String) of the parent organization (Holding).
     * @param pageable Pagination and sorting constraints.
     * @return A {@link Flux} streaming the sub-tenant {@link Tenant} aggregates.
     */
    @Nonnull
    Flux<Tenant> findByParentId(@Nonnull String parentId, @Nonnull Pageable pageable);
}