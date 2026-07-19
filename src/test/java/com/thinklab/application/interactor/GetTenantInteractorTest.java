package com.thinklab.application.interactor;

import com.thinklab.application.port.out.TenantRepositoryPort;
import com.thinklab.application.usecase.query.GetTenantQuery;
import com.thinklab.domain.exception.TenantNotFoundException;
import com.thinklab.domain.model.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetTenantInteractorTest {

    @Mock
    private TenantRepositoryPort tenantRepository;

    @Mock
    private GetTenantQuery query;

    private GetTenantInteractor interactor;

    @BeforeEach
    void setUp() {
        interactor = new GetTenantInteractor(tenantRepository);
    }

    @Test
    @DisplayName("Should successfully retrieve tenant when it exists")
    void shouldGetTenantSuccessfully() {
        // Given
        var tenantId = UUID.randomUUID().toString();
        var tenant = mock(Tenant.class);

        when(query.tenantId()).thenReturn(tenantId);
        when(tenantRepository.findById(tenantId)).thenReturn(Mono.just(tenant));
        when(tenant.id()).thenReturn(UUID.fromString(tenantId));

        // When
        Mono<Tenant> result = interactor.execute(query);

        // Then
        StepVerifier.create(result)
                .expectNext(tenant)
                .verifyComplete();

        verify(tenantRepository, times(1)).findById(tenantId);
    }

    @Test
    @DisplayName("Should throw TenantNotFoundException when tenant does not exist")
    void shouldThrowTenantNotFoundExceptionWhenNotFound() {
        // Given
        var tenantId = UUID.randomUUID().toString();

        when(query.tenantId()).thenReturn(tenantId);
        when(tenantRepository.findById(tenantId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(interactor.execute(query))
                .expectError(TenantNotFoundException.class)
                .verify();

        verify(tenantRepository, times(1)).findById(tenantId);
    }

    @Test
    @DisplayName("Should throw NullPointerException when query is null")
    void shouldThrowExceptionWhenQueryIsNull() {
        // When / Then
        assertThrows(NullPointerException.class, () -> interactor.execute(null));
        verifyNoInteractions(tenantRepository);
    }
}