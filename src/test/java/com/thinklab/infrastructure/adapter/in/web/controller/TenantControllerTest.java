package com.thinklab.infrastructure.adapter.in.web.controller;

import com.thinklab.application.port.in.*;
import com.thinklab.domain.model.Tenant;
import com.thinklab.infrastructure.adapter.in.web.dto.request.CreateTenantRequest;
import com.thinklab.infrastructure.adapter.in.web.dto.response.TenantResponse;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@MicronautTest(transactional = false)
@Property(name = "mongodb.uri", value = "mongodb://localhost:27017/dummy-test-db")
class TenantControllerTest {

    // Mocks de todos os UseCases
    @MockBean(CreateTenantUseCase.class) CreateTenantUseCase createTenantUseCase = Mockito.mock(CreateTenantUseCase.class);
    @MockBean(GetTenantUseCase.class) GetTenantUseCase getTenantUseCase = Mockito.mock(GetTenantUseCase.class);
    @MockBean(UpdateTenantProfileUseCase.class) UpdateTenantProfileUseCase updateProfileUseCase = Mockito.mock(UpdateTenantProfileUseCase.class);
    @MockBean(SuspendTenantUseCase.class) SuspendTenantUseCase suspendTenantUseCase = Mockito.mock(SuspendTenantUseCase.class);
    @MockBean(ReactivateTenantUseCase.class) ReactivateTenantUseCase reactivateTenantUseCase = Mockito.mock(ReactivateTenantUseCase.class);
    @MockBean(RevokeTenantUseCase.class) RevokeTenantUseCase revokeTenantUseCase = Mockito.mock(RevokeTenantUseCase.class);
    @MockBean(GetTenantAuditLogsUseCase.class) GetTenantAuditLogsUseCase auditLogsUseCase = Mockito.mock(GetTenantAuditLogsUseCase.class);

    TenantController controller;

    @BeforeEach
    void setUp() {
        // Instanciação manual para focarmos no teste unitário da lógica do controller,
        // isolando o teste do proxy de validação AOP (Hibernate Validator) do Micronaut.
        controller = new TenantController(
                createTenantUseCase,
                getTenantUseCase,
                updateProfileUseCase,
                suspendTenantUseCase,
                reactivateTenantUseCase,
                revokeTenantUseCase,
                auditLogsUseCase
        );
    }

    @Test
    @DisplayName("Should successfully provision a new tenant - HTTP 201")
    void shouldCreateTenantSuccessfully() {
        // Given
        var request = Mockito.mock(CreateTenantRequest.class, Mockito.RETURNS_DEEP_STUBS);
        when(request.name()).thenReturn("Thinklab");

        var tenantId = UUID.randomUUID();
        var tenantMock = Mockito.mock(Tenant.class, Mockito.RETURNS_DEEP_STUBS);
        when(tenantMock.id()).thenReturn(tenantId);

        when(createTenantUseCase.execute(any())).thenReturn(Mono.just(tenantMock));

        // When
        Mono<HttpResponse<TenantResponse>> responseMono = controller.create(request);

        // Then
        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertEquals(HttpStatus.CREATED, response.getStatus());
                    assertNotNull(response.body());
                    // Corrigido: comparando UUID com UUID
                    assertEquals(tenantId, response.body().id());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should retrieve tenant by ID - HTTP 200")
    void shouldGetTenantById() {
        // Given
        var tenantId = UUID.randomUUID(); // Corrigido: mantendo como UUID
        var tenantMock = Mockito.mock(Tenant.class, Mockito.RETURNS_DEEP_STUBS);
        when(tenantMock.id()).thenReturn(tenantId);

        when(getTenantUseCase.execute(any())).thenReturn(Mono.just(tenantMock));

        // When
        // Passando para String apenas na chamada da rota, como o Controller exige
        Mono<HttpResponse<TenantResponse>> responseMono = controller.getById(tenantId.toString());

        // Then
        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatus());
                    assertNotNull(response.body());
                    // Corrigido: comparando UUID com UUID
                    assertEquals(tenantId, response.body().id());
                })
                .verifyComplete();
    }
}