package com.thinklab.application.port.in;

import com.thinklab.application.usecase.command.ReactivateTenantCommand;
import com.thinklab.domain.model.Tenant;
import jakarta.annotation.Nonnull;
import reactor.core.publisher.Mono;

/**
 * Application Port: Input boundary for reactivating a SUSPENDED Organization (Tenant).
 * This interface defines the formal contract for restoring a tenant's operational
 * status to ACTIVE. Implementation must strictly validate the state transition
 * through the Domain State Machine and ensure a mandatory forensic audit record
 * is committed for security compliance.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 *     <li><b>State Machine Integrity:</b> Enforces that only valid suspended entities can be reactivated.</li>
 *     <li><b>Forensic Accountability:</b> Mandates business justification capture at the entry point.</li>
 *     <li><b>Reactive Protocol:</b> Utilizes {@link Mono} to handle the transactional workflow non-blockingly.</li>
 *     <li><b>Atomic Transition:</b> Ensures state mutation and audit log are functionally chained.</li>
 * </ul>
 *
 * @author Thinklab Engineering
 * @version 1.0.0
 */
public interface ReactivateTenantUseCase {

    /**
     * Executes the orchestration pipeline to restore a tenant to ACTIVE status.
     *
     * @param command The validated {@link ReactivateTenantCommand} containing ID, executor, and reason.
     * @return A {@link Mono} emitting the mutated {@link Tenant} in its new ACTIVE state.
     * @throws NullPointerException if the provided command is null, preserving pipeline integrity (Fail-Fast).
     * @apiNote Emits a {@code BusinessException} signal if the organization is not found or
     *          if the status transition violates domain invariants (e.g., reactivating an ARCHIVED tenant).
     */
    @Nonnull
    Mono<Tenant> execute(@Nonnull ReactivateTenantCommand command);
}