package com.thinklab.infrastructure.adapter.in.web.dto.response;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Infrastructure DTO: Standardized wrapper for paginated Tenant registry results.
 * This record follows the Collection Projection pattern to provide a consistent
 * structure for API consumers, including both the payload and the pagination
 * metadata required for advanced UI navigation and server-side cursor management.
 *
 * <p><b>Architectural Principles:</b></p>
 * <ul>
 *     <li><b>Protocol Consistency:</b> Ensures every listing endpoint follows the same response contract.</li>
 *     <li><b>Metadata Integrity:</b> Explicitly carries result set sizing for client-side pagination.</li>
 *     <li><b>AOT Optimized:</b> Designed for reflection-free serialization in high-throughput environments.</li>
 * </ul>
 *
 * @param content       The sanitized list of tenant records for the current requested page.
 * @param totalElements The total count of existing tenants matching the filter criteria.
 * @param page          The current zero-indexed page number.
 * @param size          The total number of items per page (page limit).
 */
@Serdeable
@Introspected
@Schema(
        name = "PagedTenantResponse",
        description = "Paginated container representing a subset of the Organization registry with metadata."
)
public record PagedTenantResponse(
        @Schema(description = "The list of organization records for the current page")
        List<TenantResponse> content,

        @Schema(description = "The global count of matching organizations", example = "150")
        long totalElements,

        @Schema(description = "Zero-based index of the current page", example = "0")
        int page,

        @Schema(description = "The volume of records requested per page", example = "20")
        int size
) {

    /**
     * Factory Method: Standardized creation of a paginated response.
     *
     * @param content       The list of projected tenants.
     * @param totalElements Total records found in the registry.
     * @param page          The current page index.
     * @param size          The items per page.
     * @return A fully initialized {@link PagedTenantResponse} instance.
     */
    public static PagedTenantResponse of(
            @NonNull List<TenantResponse> content,
            long totalElements,
            int page,
            int size
    ) {
        return new PagedTenantResponse(content, totalElements, page, size);
    }
}