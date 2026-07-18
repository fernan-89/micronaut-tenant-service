package com.thinklab.infrastructure.adapter.in.web.dto.response;

import com.thinklab.domain.model.Tenant;
import com.thinklab.domain.valueobject.TenantStatus;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO: Public-facing projection of an Organization (Tenant).
 * This record follows the Projection Pattern to transform the internal Domain Aggregate
 * into a sanitized representation, supporting global hierarchy and localization.
 *
 * <p><b>Architectural Principles:</b></p>
 * <ul>
 *     <li><b>Domain Isolation:</b> Prevents leakage of internal aggregate logic to the Web.</li>
 *     <li><b>AOT Optimized:</b> Reflection-free serialization via Micronaut Serde.</li>
 *     <li><b>Global Scale:</b> Explicitly exposes hierarchy and localization metadata.</li>
 * </ul>
 *
 * @param id        Unique system identifier (UUID).
 * @param parentId  Identifier of the parent organization (Holding). Null for root nodes.
 * @param name      Official legal or trade name.
 * @param status    Current operational lifecycle state.
 * @param profile   Consolidated localization and tax identification metadata.
 * @param createdAt UTC timestamp of initial provisioning.
 * @param updatedAt UTC timestamp of the last administrative mutation.
 * @param version   Concurrency control version for consistency tracking.
 */
@Serdeable
@Introspected
@Schema(
        name = "TenantResponse",
        description = "Standardized response payload representing an organization's state and its global localization metadata."
)
public record TenantResponse(
        @NonNull @Schema(description = "Internal unique ID", example = "79e83370-d699-4d28-817f-acc401490ed5")
        UUID id,

        @Nullable @Schema(description = "Parent organization ID (Holding link)", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID parentId,

        @NonNull @Schema(description = "Organization name", example = "Novelis Global Corp")
        String name,

        @NonNull @Schema(description = "Operational status", example = "ACTIVE")
        TenantStatus status,

        @NonNull @Schema(description = "Consolidated legal and localization profile")
        TenantProfileResponse profile,

        @NonNull @Schema(description = "Timestamp of creation")
        Instant createdAt,

        @Nullable @Schema(description = "Timestamp of last update")
        Instant updatedAt,

        @NonNull @Schema(description = "Optimistic locking version", example = "0")
        Long version
) {

    /**
     * Nested Response DTO: Projects the TenantProfile Value Object.
     */
    @Serdeable
    public record TenantProfileResponse(
            @Schema(example = "BR-77889900000199") String taxId,
            @Schema(example = "admin@novelis.com") String corporateEmail,
            @Schema(example = "BR") String countryCode,
            @Schema(example = "Sao Paulo") String city,
            @Schema(example = "America/Sao_Paulo") String timezone,
            @Schema(example = "pt") String language,
            @Schema(example = "TECHNOLOGY") String industry
    ) {}

    /**
     * Factory: Maps a Domain Aggregate into this Infrastructure Response DTO.
     *
     * @param domain The pure Tenant aggregate root.
     * @return A sanitized TenantResponse ready for serialization.
     */
    public static TenantResponse fromDomain(@NonNull Tenant domain) {
        var p = domain.profile();
        var profileResponse = new TenantProfileResponse(
                p.taxId(),
                p.corporateEmail(),
                p.countryCode(),
                p.city(),
                p.timezone(),
                p.language(),
                p.getIndustrySafely()
        );

        return new TenantResponse(
                domain.id(),
                domain.parentId(),
                domain.name(),
                domain.status(),
                profileResponse,
                domain.createdAt(),
                domain.updatedAt(),
                domain.version()
        );
    }
}