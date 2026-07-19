package com.thinklab.application.interactor;

import com.thinklab.application.port.out.TenantAuditRepositoryPort;
import com.thinklab.application.port.out.TenantRepositoryPort;
import com.thinklab.application.usecase.command.ReactivateTenantCommand;
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
class ReactivateTenantInteractorTest {

    @Mock
    private TenantRepositoryPort tenantRepository;

    @Mock
    private TenantAuditRepositoryPort auditRepository;

    private ReactivateTenantInteractor interactor;

    @BeforeEach
    void setUp() {
        interactor = new ReactivateTenantInteractor(tenantRepository, auditRepository);
    }

    @Test
    @DisplayName("Should successfully reactivate tenant and register audit")
    void shouldReactivateTenantSuccessfully() {
        // Given
        var tenantId = UUID.randomUUID();
        var executor = "admin-user";
        // Adicionado o terceiro parâmetro exigido pelo record
        var command = new ReactivateTenantCommand(tenantId.toString(), executor, "TEST_REACTIVATION_REASON");

        var tenant = mock(Tenant.class);
        var reactivatedTenant = mock(Tenant.class);

        // Configuração do Tenant reativado (usado no update e auditoria)
        when(reactivatedTenant.id()).thenReturn(tenantId);
        when(reactivatedTenant.parentId()).thenReturn(null); // Assumindo Root Tenant
        when(reactivatedTenant.isBranch()).thenReturn(false);

        // Simula o comportamento do domínio
        when(tenantRepository.findById(command.tenantId())).thenReturn(Mono.just(tenant));
        when(tenant.reactivate(executor)).thenReturn(reactivatedTenant);

        // Simula a persistência e a auditoria
        when(tenantRepository.update(any(Tenant.class))).thenReturn(Mono.just(reactivatedTenant));
        when(auditRepository.save(any(TenantAudit.class))).thenReturn(Mono.just(mock(TenantAudit.class)));

        // When
        var result = interactor.execute(command);

        // Then
        StepVerifier.create(result)
                .expectNext(reactivatedTenant)
                .verifyComplete();

        verify(tenantRepository).findById(command.tenantId());
        verify(tenantRepository).update(any(Tenant.class));
        verify(auditRepository).save(any(TenantAudit.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when tenant is not found")
    void shouldThrowBusinessExceptionWhenNotFound() {
        // Given
        // Adicionado o terceiro parâmetro exigido pelo record
        var command = new ReactivateTenantCommand(UUID.randomUUID().toString(), "admin-user", "TEST_REASON");

        when(tenantRepository.findById(command.tenantId())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(interactor.execute(command))
                .expectError(BusinessException.class)
                .verify();

        verify(tenantRepository, never()).update(any());
        verify(auditRepository, never()).save(any());
    }
}