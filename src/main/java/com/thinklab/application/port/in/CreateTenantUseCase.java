package com.thinklab.application.port.in;

import com.thinklab.application.usecase.command.CreateTenantCommand;
import com.thinklab.domain.model.Tenant;
import jakarta.annotation.Nonnull;
import reactor.core.publisher.Mono;

/**
 * Application Port: Input boundary for provisioning new Organizations (Tenants).
 * This interface defines the formal contract for establishing organizational
 * identities within the Global SaaS ecosystem. Implementation is responsible
 * for orchestrating deterministic identification, persistence, and mandatory
 * forensic audit registration.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 *     <li><b>Reactive Sovereignty:</b> Strictly utilizes {@link Mono} to ensure non-blocking, asynchronous execution.</li>
 *     <li><b>Deterministic Idempotency:</b> Enforces identification rules to prevent duplicate organizational provisioning.</li>
 *     <li><b>Forensic Traceability:</b> Mandates that every successful creation is permanently recorded in the audit trail.</li>
 *     <li><b>Domain Isolation:</b> Shields business logic from transport protocols via the command pattern.</li>
 * </ul>
 *
 * @author Thinklab Engineering
 * @version 1.0.0
 */
public interface CreateTenantUseCase {

    /**
     * Executes the orchestration pipeline to provision a new organization.
     * <p>The workflow includes TaxID uniqueness verification, deterministic UUID
     * generation, atomic state persistence, and forensic event logging.</p>
     *
     * @param command The validated {@link CreateTenantCommand} containing organizational metadata and executor context.
     * @return A {@link Mono} emitting the successfully provisioned {@link Tenant} aggregate.
     * @throws NullPointerException if the provided command is null, preserving pipeline integrity (Fail-Fast).
     * @apiNote Emits a {@code BusinessException} signal (e.g., TENANT_DUPLICATE) through the reactive stream if
     *          organizational invariants or uniqueness rules are violated.
     */
    @Nonnull
    Mono<Tenant> execute(@Nonnull CreateTenantCommand command);
}