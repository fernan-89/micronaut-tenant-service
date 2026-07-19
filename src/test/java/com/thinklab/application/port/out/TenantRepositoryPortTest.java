package com.thinklab.application.port.out;

import com.thinklab.domain.model.Tenant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantRepositoryPortTest {

    @Mock
    private TenantRepositoryPort tenantRepositoryPort;

    @Test
    @DisplayName("Should successfully mock save operation for Tenant")
    void shouldMockSaveTenant() {
        // Given
        var tenantId = UUID.randomUUID();
        var tenantMock = Mockito.mock(Tenant.class);
        when(tenantMock.id()).thenReturn(tenantId);

        when(tenantRepositoryPort.save(any(Tenant.class))).thenReturn(Mono.just(tenantMock));

        // When
        Mono<Tenant> result = tenantRepositoryPort.save(tenantMock);

        // Then
        StepVerifier.create(result)
                .assertNext(savedTenant -> {
                    assertNotNull(savedTenant);
                    assertEquals(tenantId, savedTenant.id());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should successfully mock findById operation")
    void shouldMockFindById() {
        // Given
        var tenantId = UUID.randomUUID();
        var tenantMock = Mockito.mock(Tenant.class);
        when(tenantMock.id()).thenReturn(tenantId);

        // CORREÇÃO AQUI: Convertendo UUID para String conforme esperado pela interface
        when(tenantRepositoryPort.findById(tenantId.toString())).thenReturn(Mono.just(tenantMock));

        // When
        Mono<Tenant> result = tenantRepositoryPort.findById(tenantId.toString());

        // Then
        StepVerifier.create(result)
                .assertNext(foundTenant -> {
                    assertEquals(tenantId, foundTenant.id());
                })
                .verifyComplete();
    }
}