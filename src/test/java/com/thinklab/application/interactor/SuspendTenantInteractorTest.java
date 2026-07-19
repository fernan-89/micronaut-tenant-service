package com.thinklab.application.interactor;

import com.thinklab.application.port.out.TenantAuditRepositoryPort;
import com.thinklab.application.port.out.TenantRepositoryPort;
import com.thinklab.application.usecase.command.SuspendTenantCommand;
import com.thinklab.domain.exception.BusinessException;
import com.thinklab.domain.model.Tenant;
import com.thinklab.domain.model.TenantAudit;
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
class SuspendTenantInteractorTest {

    @Mock
    private TenantRepositoryPort tenantRepository;

    @Mock
    private TenantAuditRepositoryPort auditRepository;

    private SuspendTenantInteractor interactor;

    @BeforeEach
    void setUp() {
        interactor = new SuspendTenantInteractor(tenantRepository, auditRepository);
    }

    @Test
    @DisplayName("Should successfully suspend tenant and register audit")
    void shouldSuspendTenantSuccessfully() {
        // Given
        var tenantId = UUID.randomUUID();
        var executor = "admin-user";
        // Corrigido para incluir os 3 argumentos exigidos pelo record SuspendTenantCommand
        var command = new SuspendTenantCommand(tenantId.toString(), executor, "TEST_SUSPENSION_REASON");

        var tenant = mock(Tenant.class);
        var suspendedTenant = mock(Tenant.class);

        // Configuração do Tenant suspenso (usado no update e auditoria)
        when(suspendedTenant.id()).thenReturn(tenantId);
        when(suspendedTenant.parentId()).thenReturn(null); // Assumindo Root Tenant
        when(suspendedTenant.isBranch()).thenReturn(false);

        // Simula o comportamento do domínio
        when(tenantRepository.findById(command.tenantId())).thenReturn(Mono.just(tenant));
        when(tenant.suspend(executor)).thenReturn(suspendedTenant);

        // Simula a persistência e a auditoria
        when(tenantRepository.update(any(Tenant.class))).thenReturn(Mono.just(suspendedTenant));
        when(auditRepository.save(any(TenantAudit.class))).thenReturn(Mono.just(mock(TenantAudit.class)));

        // When
        var result = interactor.execute(command);

        // Then
        StepVerifier.create(result)
                .expectNext(suspendedTenant)
                .verifyComplete();

        verify(tenantRepository).findById(command.tenantId());
        verify(tenantRepository).update(any(Tenant.class));
        verify(auditRepository).save(any(TenantAudit.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when tenant is not found")
    void shouldThrowBusinessExceptionWhenNotFound() {
        // Given
        // Corrigido para incluir os 3 argumentos exigidos pelo record SuspendTenantCommand
        var command = new SuspendTenantCommand(UUID.randomUUID().toString(), "admin-user", "TEST_REASON");

        when(tenantRepository.findById(command.tenantId())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(interactor.execute(command))
                .expectError(BusinessException.class)
                .verify();

        verify(tenantRepository, never()).update(any());
        verify(auditRepository, never()).save(any());
    }
}