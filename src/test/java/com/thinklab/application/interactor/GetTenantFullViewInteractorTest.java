package com.thinklab.application.interactor;

import com.thinklab.application.port.in.GetTenantFullViewUseCase.TenantFullView;
import com.thinklab.application.port.out.TenantAuditRepositoryPort;
import com.thinklab.application.port.out.TenantRepositoryPort;
import com.thinklab.domain.exception.BusinessException;
import com.thinklab.domain.model.Tenant;
import com.thinklab.domain.model.TenantAudit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetTenantFullViewInteractorTest {

    @Mock
    private TenantRepositoryPort tenantRepository;

    @Mock
    private TenantAuditRepositoryPort auditRepository;

    private GetTenantFullViewInteractor interactor;

    @BeforeEach
    void setUp() {
        interactor = new GetTenantFullViewInteractor(tenantRepository, auditRepository);
    }

    @Test
    @DisplayName("Should successfully retrieve tenant full view with audit trail")
    void shouldGetTenantFullViewSuccessfully() {
        // Given
        var tenantId = UUID.randomUUID().toString();
        var tenant = mock(Tenant.class);
        var auditEntry = mock(TenantAudit.class);

        when(tenant.id()).thenReturn(UUID.fromString(tenantId));
        when(tenantRepository.findById(tenantId)).thenReturn(Mono.just(tenant));
        when(auditRepository.findByTenantId(tenantId)).thenReturn(Flux.just(auditEntry));

        // When
        var result = interactor.execute(tenantId);

        // Then
        StepVerifier.create(result)
                .assertNext(view -> {
                    assertEquals(tenant, view.tenant());
                    assertEquals(1, view.auditLogs().size());
                    assertEquals(auditEntry, view.auditLogs().get(0));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should throw BusinessException when tenant is not found")
    void shouldThrowBusinessExceptionWhenNotFound() {
        // Given
        var tenantId = UUID.randomUUID().toString();
        when(tenantRepository.findById(tenantId)).thenReturn(Mono.empty());

        // When & Then
        // Aqui validamos se é a exceção correta sem precisar do getCode()
        StepVerifier.create(interactor.execute(tenantId))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException)
                .verify();

        verify(tenantRepository, times(1)).findById(tenantId);
        verify(auditRepository, never()).findByTenantId(anyString());
    }
}