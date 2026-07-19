package com.thinklab.application.interactor;

import com.thinklab.application.port.out.TenantAuditRepositoryPort;
import com.thinklab.application.port.out.TenantRepositoryPort;
import com.thinklab.application.usecase.command.UpdateTenantProfileCommand;
import com.thinklab.domain.exception.BusinessException;
import com.thinklab.domain.model.Tenant;
import com.thinklab.domain.model.TenantAudit;
import com.thinklab.domain.valueobject.TenantProfile;
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
class UpdateTenantProfileInteractorTest {

    @Mock
    private TenantRepositoryPort tenantRepository;

    @Mock
    private TenantAuditRepositoryPort auditRepository;

    private UpdateTenantProfileInteractor interactor;

    @BeforeEach
    void setUp() {
        interactor = new UpdateTenantProfileInteractor(tenantRepository, auditRepository);
    }

    @Test
    @DisplayName("Should successfully update profile and register audit")
    void shouldUpdateProfileSuccessfully() {
        // Given
        var tenantId = UUID.randomUUID();
        var command = new UpdateTenantProfileCommand(
                tenantId.toString(),
                "executor-123",
                new TenantProfile("TAX", "email@test.com", "BR", "SP", "UTC", "pt", "TECH")
        );

        var tenant = mock(Tenant.class);
        var updatedTenant = mock(Tenant.class);

        // A linha que causava a exceção (when(tenant.id())...) foi removida,
        // pois o Interactor não chama .id() no tenant original.

        // Configuração do Tenant atualizado (usado no update e auditoria)
        when(updatedTenant.id()).thenReturn(tenantId);
        when(updatedTenant.parentId()).thenReturn(null); // Assume tenant raiz
        when(updatedTenant.isBranch()).thenReturn(false); // Assume tenant raiz

        // Simula o comportamento do domínio
        when(tenantRepository.findById(command.tenantId())).thenReturn(Mono.just(tenant));
        when(tenant.updateProfile(command.profile(), command.executor())).thenReturn(updatedTenant);

        // Simula a persistência e a auditoria
        when(tenantRepository.update(any(Tenant.class))).thenReturn(Mono.just(updatedTenant));
        when(auditRepository.save(any(TenantAudit.class))).thenReturn(Mono.just(mock(TenantAudit.class)));

        // When
        var result = interactor.execute(command);

        // Then
        StepVerifier.create(result)
                .expectNext(updatedTenant)
                .verifyComplete();

        verify(tenantRepository).findById(command.tenantId());
        verify(tenantRepository).update(any(Tenant.class));
        verify(auditRepository).save(any(TenantAudit.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when tenant is not found")
    void shouldThrowBusinessExceptionWhenNotFound() {
        // Given
        var command = new UpdateTenantProfileCommand(
                UUID.randomUUID().toString(),
                "executor-123",
                new TenantProfile("TAX", "email@test.com", "BR", "SP", "UTC", "pt", "TECH")
        );

        when(tenantRepository.findById(command.tenantId())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(interactor.execute(command))
                .expectError(BusinessException.class)
                .verify();

        verify(tenantRepository, never()).update(any());
        verify(auditRepository, never()).save(any());
    }
}