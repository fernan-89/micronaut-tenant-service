package com.thinklab.application.port.in;

import com.thinklab.application.usecase.command.SuspendTenantCommand;
import com.thinklab.domain.model.Tenant;
import jakarta.annotation.Nonnull;
import reactor.core.publisher.Mono;

/**
 * Application Port: Input boundary for suspending an active Organization (Tenant).
 * This interface defines the contract for halting tenant operations. Implementation
 * must ensure that state transitions follow domain invariants and that a mandatory
 * forensic audit record is persisted before the operation completes.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 *     <li><b>State Integrity:</b> Enforces transitions strictly via the Domain State Machine.</li>
 *     <li><b>Defensive Boundary:</b> Requires a validated {@link SuspendTenantCommand} to prevent malformed execution.</li>
 *     <li><b>Reactive Sovereignty:</b> Ensures non-blocking execution through the {@link Mono} pipeline.</li>
 * </ul>
 *
 * @version 1.0.0
 */
public interface SuspendTenantUseCase {

    /**
     * Executes the orchestration pipeline to suspend a tenant's operational status.
     *
     * @param command The validated {@link SuspendTenantCommand} containing target ID, executor, and reason.
     * @return A {@link Mono} emitting the mutated {@link Tenant} in its new SUSPENDED state.
     * @throws NullPointerException if the provided command is null (Fail-Fast).
     * @apiNote Emits a {@code BusinessException} signal if the transition is illegal or the tenant is not found.
     */
    @Nonnull
    Mono<Tenant> execute(@Nonnull SuspendTenantCommand command);
}