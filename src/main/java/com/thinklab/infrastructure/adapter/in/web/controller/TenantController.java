package com.thinklab.infrastructure.adapter.in.web.controller;

import com.thinklab.application.port.in.*;
import com.thinklab.application.usecase.query.GetTenantQuery;
import com.thinklab.infrastructure.adapter.in.web.dto.request.*;
import com.thinklab.infrastructure.adapter.in.web.dto.response.*;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.http.exceptions.HttpStatusException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * REST Controller: Primary inbound adapter for Organization management.
 * Acts as a protocol translator between HTTP/JSON and Application Use Cases.
 * It strictly enforces boundary validation and projects domain aggregates into
 * sanitized response DTOs to prevent data leakage.
 */
@Slf4j
@Controller("/tenants")
@Tag(name = "Administrative Center", description = "Endpoints for managing Organization lifecycles and metadata.")
public class TenantController {

    private final CreateTenantUseCase createTenantUseCase;
    private final GetTenantUseCase getTenantUseCase;
    private final UpdateTenantProfileUseCase updateProfileUseCase;
    private final SuspendTenantUseCase suspendTenantUseCase;
    private final ReactivateTenantUseCase reactivateTenantUseCase;
    private final RevokeTenantUseCase revokeTenantUseCase;
    private final GetTenantAuditLogsUseCase auditLogsUseCase;

    /**
     * Explicit constructor injection to prevent Micronaut / Lombok proxy generation issues (NoSuchMethodError).
     */
    @Inject
    public TenantController(
            CreateTenantUseCase createTenantUseCase,
            GetTenantUseCase getTenantUseCase,
            UpdateTenantProfileUseCase updateProfileUseCase,
            SuspendTenantUseCase suspendTenantUseCase,
            ReactivateTenantUseCase reactivateTenantUseCase,
            RevokeTenantUseCase revokeTenantUseCase,
            GetTenantAuditLogsUseCase auditLogsUseCase) {
        this.createTenantUseCase = createTenantUseCase;
        this.getTenantUseCase = getTenantUseCase;
        this.updateProfileUseCase = updateProfileUseCase;
        this.suspendTenantUseCase = suspendTenantUseCase;
        this.reactivateTenantUseCase = reactivateTenantUseCase;
        this.revokeTenantUseCase = revokeTenantUseCase;
        this.auditLogsUseCase = auditLogsUseCase;
    }

    /**
     * Provisions a new organization (Holding or Branch).
     */
    @Post
    @Operation(summary = "Provision a new Tenant", description = "Establishes a new organization with deterministic identification and forensic auditing.")
    @ApiResponse(responseCode = "201", description = "Organization provisioned successfully.")
    @ApiResponse(responseCode = "400", description = "Invalid request payload or malformed metadata.")
    public Mono<HttpResponse<TenantResponse>> create(@Body @Valid CreateTenantRequest request) {
        return createTenantUseCase.execute(request.toCommand())
                .map(TenantResponse::fromDomain)
                .map(body -> (HttpResponse<TenantResponse>) HttpResponse.created(body))
                .doOnSubscribe(s -> log.info("[ACTION: CREATE_TENANT] [NAME: {}] - Initiating provisioning sequence.", request.name()))
                .doOnSuccess(res -> log.info("[ACTION: CREATE_TENANT] [ID: {}] - Provisioning completed successfully.", res.body().id()))
                .doOnError(err -> log.error("[ACTION: CREATE_TENANT] - Provisioning failed: {}", err.getMessage()));
    }

    /**
     * Retrieves the current state of an organization.
     */
    @Get("/{id}")
    @Operation(summary = "Get Tenant by ID", description = "Retrieves the current metadata and status of a specific organization.")
    @ApiResponse(responseCode = "200", description = "Tenant found and projected.")
    @ApiResponse(responseCode = "404", description = "Tenant not found.")
    public Mono<HttpResponse<TenantResponse>> getById(
            @PathVariable @Parameter(description = "UUID of the target tenant") String id) {
        return getTenantUseCase.execute(new GetTenantQuery(id))
                .map(TenantResponse::fromDomain)
                .map(body -> (HttpResponse<TenantResponse>) HttpResponse.ok(body))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new HttpStatusException(HttpStatus.NOT_FOUND, "Tenant not found."))))
                .doOnSubscribe(s -> log.info("[ACTION: GET_TENANT] [ID: {}] - Retrieving state.", id))
                .doOnError(err -> log.error("[ACTION: GET_TENANT] [ID: {}] - Retrieval failed: {}", id, err.getMessage()));
    }

    /**
     * Updates organizational metadata (TaxID, Location, Contacts).
     */
    @Put("/{id}/profile")
    @Operation(summary = "Update Tenant profile", description = "Mutates organizational metadata. This operation is fully audited.")
    @ApiResponse(responseCode = "200", description = "Profile updated successfully.")
    public Mono<HttpResponse<TenantResponse>> updateProfile(
            @PathVariable String id, @Body @Valid UpdateTenantProfileRequest request) {
        return updateProfileUseCase.execute(request.toCommand(id))
                .map(TenantResponse::fromDomain)
                .map(body -> (HttpResponse<TenantResponse>) HttpResponse.ok(body))
                .doOnSubscribe(s -> log.info("[ACTION: UPDATE_PROFILE] [ID: {}] [EXECUTOR: {}] - Initiating metadata mutation.", id, request.executor()))
                .doOnSuccess(res -> log.info("[ACTION: UPDATE_PROFILE] [ID: {}] - Metadata successfully updated.", id));
    }

    /**
     * Halts organization operations (SUSPENDED state).
     */
    @Patch("/{id}/suspend")
    @Operation(summary = "Suspend Tenant", description = "Temporarily interrupts organization operations. Requires business justification.")
    public Mono<HttpResponse<TenantResponse>> suspend(
            @PathVariable String id, @Body @Valid SuspendTenantRequest request) {
        return suspendTenantUseCase.execute(request.toCommand(id))
                .map(TenantResponse::fromDomain)
                .map(body -> (HttpResponse<TenantResponse>) HttpResponse.ok(body))
                .doOnSubscribe(s -> log.info("[ACTION: SUSPEND_TENANT] [ID: {}] - Initiating operational halt.", id))
                .doOnSuccess(res -> log.info("[ACTION: SUSPEND_TENANT] [ID: {}] - Organization suspended.", id));
    }

    /**
     * Restores organization operations (ACTIVE state).
     */
    @Patch("/{id}/reactivate")
    @Operation(summary = "Reactivate Tenant", description = "Restores an organization to ACTIVE status after suspension.")
    public Mono<HttpResponse<TenantResponse>> reactivate(
            @PathVariable String id, @Body @Valid ReactivateTenantRequest request) {
        return reactivateTenantUseCase.execute(request.toCommand(id))
                .map(TenantResponse::fromDomain)
                .map(body -> (HttpResponse<TenantResponse>) HttpResponse.ok(body))
                .doOnSubscribe(s -> log.info("[ACTION: REACTIVATE_TENANT] [ID: {}] - Restoring operations.", id))
                .doOnSuccess(res -> log.info("[ACTION: REACTIVATE_TENANT] [ID: {}] - Organization reactivated.", id));
    }

    /**
     * Irreversibly decommission an organization (REVOKED state).
     */
    @Delete("/{id}")
    @Operation(summary = "Revoke Tenant", description = "Permanently and irreversibly revokes an organization under Zero Trust principles.")
    @ApiResponse(responseCode = "200", description = "Organization permanently decommissioned.")
    public Mono<HttpResponse<TenantResponse>> revoke(
            @PathVariable String id, @Body @Valid RevokeTenantRequest request) {
        return revokeTenantUseCase.execute(request.toCommand(id))
                .map(TenantResponse::fromDomain)
                .map(body -> (HttpResponse<TenantResponse>) HttpResponse.ok(body))
                .doOnSubscribe(s -> log.warn("[ACTION: REVOKE_TENANT] [ID: {}] [EXECUTOR: {}] - CRITICAL: Initiating irreversible decommissioning.", id, request.executor()))
                .doOnSuccess(res -> log.warn("[ACTION: REVOKE_TENANT] [ID: {}] - CRITICAL: Organization successfully revoked.", id));
    }

    /**
     * Streams the chronological forensic audit trail.
     */
    @Get("/{id}/audit")
    @Operation(summary = "Get Audit Trail", description = "Streams the immutable history of all state mutations for the target organization.")
    public Mono<HttpResponse<List<TenantAuditResponse>>> getAuditTrail(@PathVariable String id) {
        return auditLogsUseCase.execute(id)
                .map(TenantAuditResponse::fromDomain)
                .collectList()
                .map(body -> (HttpResponse<List<TenantAuditResponse>>) HttpResponse.ok(body))
                .doOnSubscribe(s -> log.info("[ACTION: GET_AUDIT] [ID: {}] - Extracting forensic history.", id));
    }
}