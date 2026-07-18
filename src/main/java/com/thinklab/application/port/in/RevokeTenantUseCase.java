package com.thinklab.application.port.in;

import com.thinklab.application.usecase.command.RevokeTenantCommand;
import com.thinklab.domain.model.Tenant;
import jakarta.annotation.Nonnull;
import reactor.core.publisher.Mono;

/**
 * Application Port: Input boundary for permanently revoking an Organization (Tenant).
 * This interface represents the terminal entry point in the Tenant lifecycle.
 * Revocation is an irreversible action under the Zero Trust principle, ensuring
 * that once an organization is revoked, it cannot be transitioned back to
 * any other operational state (ACTIVE or SUSPENDED).
 *
 * <p>Implementation is responsible for orchestrating the final state mutation,
 * ensuring that all security-critical metadata and business justifications
 * are recorded in the immutable forensic audit trail.</p>
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 *     <li><b>Terminal State Integrity:</b> Enforces the finality of the revocation process.</li>
 *     <li><b>Forensic Accountability:</b> Mandates a business reason for compliance auditing.</li>
 *     <li><b>Reactive Sovereignty:</b> Strictly utilizes {@link Mono} for non-blocking execution.</li>
 *     <li><b>Domain Isolation:</b> Shields the core from transport specifics via command objects.</li>
 * </ul>
 *
 * @author Thinklab Engineering
 * @version 1.0.0
 */
public interface RevokeTenantUseCase {

    /**
     * Executes the orchestration pipeline to permanently revoke an organization.
     * <p>This operation involves validating the current state, committing the terminal
     * status change to the persistent registry, and generating a mandatory
     * forensic audit record with elevated severity.</p>
     *
     * @param command The validated {@link RevokeTenantCommand} containing target ID, executor, and reason.
     * @return A {@link Mono} emitting the mutated {@link Tenant} in its terminal REVOKED state.
     * @throws NullPointerException if the provided command is null (Fail-Fast).
     * @apiNote Emits a {@code BusinessException} (e.g., TENANT_NOT_FOUND) through the
     *          reactive stream if the entity does not exist or violates state invariants.
     */
    @Nonnull
    Mono<Tenant> execute(@Nonnull RevokeTenantCommand command);
}