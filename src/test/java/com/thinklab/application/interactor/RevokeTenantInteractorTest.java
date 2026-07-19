package com.thinklab.application.interactor;

import com.thinklab.application.port.out.TenantAuditRepositoryPort;
import com.thinklab.application.port.out.TenantRepositoryPort;
import com.thinklab.application.usecase.command.RevokeTenantCommand;
import com.thinklab.domain.exception.BusinessException;
import com.thinklab.domain.model.Tenant;
import com.thinklab.domain.model.TenantAudit;
import com.thinklab.domain.valueobject.TenantStatus; // Import corrigido
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevokeTenantInteractorTest {

    @Mock
    private TenantRepositoryPort tenantRepository;

    @Mock
    private TenantAuditRepositoryPort auditRepository;

    private RevokeTenantInteractor interactor;

    @BeforeEach
    void setUp() {
        interactor = new RevokeTenantInteractor(tenantRepository, auditRepository);
    }

    @Test
    @DisplayName("Should successfully revoke tenant and register audit")
    void shouldRevokeTenantSuccessfully() {
        // Given
        var tenantId = UUID.randomUUID();
        var executor = "admin-user";
        var reason = "Contract termination";
        var command = new RevokeTenantCommand(tenantId.toString(), executor, reason);

        var tenant = mock(Tenant.class);
        var revokedTenant = mock(Tenant.class);

        // Configuração do Tenant revogado
        when(revokedTenant.id()).thenReturn(tenantId);

        // Mock do TenantStatus correto
        var statusMock = mock(TenantStatus.class);
        when(statusMock.name()).thenReturn("REVOKED");
        when(revokedTenant.status()).thenReturn(statusMock);

        // Simula o comportamento do domínio
        when(tenantRepository.findById(command.tenantId())).thenReturn(Mono.just(tenant));
        when(tenant.revoke(executor)).thenReturn(revokedTenant);

        // Simula a persistência e a auditoria
        when(tenantRepository.update(any(Tenant.class))).thenReturn(Mono.just(revokedTenant));
        when(auditRepository.save(any(TenantAudit.class))).thenReturn(Mono.just(mock(TenantAudit.class)));

        // When
        var result = interactor.execute(command);

        // Then
        StepVerifier.create(result)
                .expectNext(revokedTenant)
                .verifyComplete();

        verify(tenantRepository).findById(command.tenantId());
        verify(tenantRepository).update(any(Tenant.class));
        verify(auditRepository).save(any(TenantAudit.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when tenant is not found")
    void shouldThrowBusinessExceptionWhenNotFound() {
        // Given
        var command = new RevokeTenantCommand(UUID.randomUUID().toString(), "admin-user", "Reason");

        when(tenantRepository.findById(command.tenantId())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(interactor.execute(command))
                .expectError(BusinessException.class)
                .verify();

        verify(tenantRepository, never()).update(any());
        verify(auditRepository, never()).save(any());
    }
}