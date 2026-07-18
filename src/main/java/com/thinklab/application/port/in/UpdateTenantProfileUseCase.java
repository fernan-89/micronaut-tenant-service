package com.thinklab.application.port.in;

import com.thinklab.application.usecase.command.UpdateTenantProfileCommand;
import com.thinklab.domain.model.Tenant;
import jakarta.annotation.Nonnull;
import reactor.core.publisher.Mono;

/**
 * Application Port: Input boundary for updating an Organization's metadata profile.
 * This interface defines the contract for orchestrating metadata mutations, ensuring
 * that localization, tax identification, and contact details are updated according
 * to domain invariants and business rules.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 *     <li><b>Protocol Decoupling:</b> Shield's the application logic from web-specific structures via the command pattern.</li>
 *     <li><b>Atomic Mutation:</b> Ensures that state updates and forensic audit entries are committed transactionally.</li>
 *     <li><b>Non-blocking Execution:</b> Fully integrated with Project Reactor to support high-concurrency environments.</li>
 * </ul>
 *
 * @author Thinklab Engineering
 * @version 1.0.0
 */
public interface UpdateTenantProfileUseCase {

    /**
     * Executes the orchestration pipeline to modify an organization's profile.
     * <p>The implementation must verify the existence of the tenant, validate the
     * requested profile changes, persist the mutated state, and generate a
     * mandatory forensic audit record.</p>
     *
     * @param command The validated {@link UpdateTenantProfileCommand} containing target ID, executor, and new profile data.
     * @return A {@link Mono} emitting the mutated {@link Tenant} instance with the updated profile.
     * @throws NullPointerException if the provided command is null (Fail-Fast).
     * @apiNote Emits a {@code BusinessException} signal (e.g., TENANT_NOT_FOUND) through the reactive pipeline if search fails.
     */
    @Nonnull
    Mono<Tenant> execute(@Nonnull UpdateTenantProfileCommand command);
}

