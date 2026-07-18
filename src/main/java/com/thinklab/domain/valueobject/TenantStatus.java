package com.thinklab.domain.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Nonnull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Value Object: Type-Safe Enumeration representing the lifecycle states of a corporate Tenant.
 * <p>Acts as a localized Finite State Machine (FSM) to enforce valid enterprise state transitions
 * at the domain boundary, preventing corruption of the system's operational lifecycle.</p>
 *
 * <p><b>Allowed Transitions:</b></p>
 * <ul>
 * <li>{@code PENDING_ACTIVATION} &rarr; {@code ACTIVE}, {@code ARCHIVED}, {@code REVOKED}</li>
 * <li>{@code ACTIVE} &rarr; {@code SUSPENDED}, {@code ARCHIVED}, {@code REVOKED}</li>
 * <li>{@code SUSPENDED} &rarr; {@code ACTIVE}, {@code ARCHIVED}, {@code REVOKED}</li>
 * <li>{@code ARCHIVED} &rarr; None (Terminal State)</li>
 * <li>{@code REVOKED} &rarr; None (Terminal State)</li>
 * </ul>
 */
public enum TenantStatus {

    /**
     * Tenant has been provisioned into the platform database but lacks required
     * cryptographic artifacts to begin operations.
     */
    PENDING_ACTIVATION("PENDING_ACTIVATION"),

    /**
     * Tenant is fully active, authenticated, and capable of executing business capabilities.
     */
    ACTIVE("ACTIVE"),

    /**
     * Tenant operations are temporarily frozen.
     */
    SUSPENDED("SUSPENDED"),

    /**
     * Tenant is permanently deactivated and soft-deleted. Terminal state.
     */
    ARCHIVED("ARCHIVED"),

    /**
     * Tenant is permanently revoked due to non-compliance or policy violations. Terminal state.
     */
    REVOKED("REVOKED");

    private final String value;

    // State machine graph definition using defensive immutable sets
    private static final Map<TenantStatus, Set<TenantStatus>> VALID_TRANSITIONS = Map.of(
            PENDING_ACTIVATION, Set.of(ACTIVE, ARCHIVED, REVOKED),
            ACTIVE, Set.of(SUSPENDED, ARCHIVED, REVOKED),
            SUSPENDED, Set.of(ACTIVE, ARCHIVED, REVOKED),
            ARCHIVED, Collections.emptySet(),
            REVOKED, Collections.emptySet()
    );

    // High-performance fail-fast lookup map
    private static final Map<String, TenantStatus> LOOKUP_MAP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    status -> status.getValue().toUpperCase(),
                    Function.identity()
            ));

    TenantStatus(String value) {
        this.value = value;
    }

    @JsonValue
    @Nonnull
    public String getValue() {
        return value;
    }

    /**
     * Evaluates whether a transition from the current state to the proposed target state is legally valid.
     */
    public void validateTransitionTo(@Nonnull TenantStatus targetProposed) {
        Set<TenantStatus> allowedTargets = VALID_TRANSITIONS.get(this);
        if (allowedTargets == null || !allowedTargets.contains(targetProposed)) {
            throw new IllegalStateException(String.format(
                    "Domain Invariant Violation: Cannot transition Tenant lifecycle from [%s] to [%s].",
                    this.name(), targetProposed.name()
            ));
        }
    }

    @JsonCreator
    @Nonnull
    public static TenantStatus fromValue(@Nonnull String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Target Tenant status string value cannot be null or blank.");
        }

        TenantStatus status = LOOKUP_MAP.get(value.trim().toUpperCase());
        if (status == null) {
            throw new IllegalArgumentException(String.format(
                    "Unknown or unsupported Tenant status value: [%s]. Valid options are %s.",
                    value, LOOKUP_MAP.keySet()
            ));
        }
        return status;
    }
}