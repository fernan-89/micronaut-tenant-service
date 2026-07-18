package com.thinklab.domain.exception;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Domain Business Exception thrown when an illegal state transition is attempted
 * on a Tenant's lifecycle (e.g., trying to reactivate a REVOKED organization).
 * This exception serves as a primary guard for the domain's State Machine,
 * ensuring that the aggregate never enters an inconsistent or prohibited state.
 *
 * <p>Following the Mission-Critical Pattern, it extends {@link BusinessException}
 * to provide a standardized "INVALID_TENANT_STATUS" error code, allowing
 * infrastructure adapters to map it correctly to HTTP 409 or 422 responses.</p>
 *
 * <p><b>Architectural Principles:</b></p>
 * <ul>
 *     <li><b>State Integrity:</b> Prevents corruption of the organizational lifecycle.</li>
 *     <li><b>Machine-Readable Errors:</b> Uses static error codes for reliable client-side handling.</li>
 *     <li><b>Defensive Instantiation:</b> Guarantees a valid exception object even with null inputs.</li>
 *     <li><b>Domain Isolation:</b> Zero dependency on web or persistence frameworks.</li>
 * </ul>
 */
public class InvalidTenantStatusException extends BusinessException {

    private static final String ERROR_CODE = "INVALID_TENANT_STATUS";
    private static final String DEFAULT_MESSAGE = "The requested lifecycle transition is prohibited by domain invariants.";

    /**
     * Constructs a new InvalidTenantStatusException with a specific compliance message.
     *
     * @param message The detailed explanation of why the state transition was rejected.
     */
    public InvalidTenantStatusException(@Nullable String message) {
        super(ERROR_CODE, (message == null || message.isBlank()) ? DEFAULT_MESSAGE : message);
    }

    /**
     * Constructs a new InvalidTenantStatusException for a specific transition failure.
     * Helper constructor to standardize reporting between states.
     *
     * @param currentStatus The status the entity is currently in.
     * @param targetStatus  The status the entity was attempting to transition to.
     */
    public static InvalidTenantStatusException forTransition(@Nonnull Object currentStatus, @Nonnull Object targetStatus) {
        String msg = String.format("Illegal state transition attempt from [%s] to [%s].", currentStatus, targetStatus);
        return new InvalidTenantStatusException(msg);
    }
}