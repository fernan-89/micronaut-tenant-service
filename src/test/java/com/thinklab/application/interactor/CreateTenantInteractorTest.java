package com.thinklab.application.interactor;

import com.thinklab.application.port.out.TenantAuditRepositoryPort;
import com.thinklab.application.port.out.TenantRepositoryPort;
import com.thinklab.application.usecase.command.CreateTenantCommand;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateTenantInteractorTest {

    @Mock
    private TenantRepositoryPort tenantRepository;

    @Mock
    private TenantAuditRepositoryPort auditRepository;

    private CreateTenantInteractor interactor;

    @BeforeEach
    void setUp() {
        interactor = new CreateTenantInteractor(tenantRepository, auditRepository);
    }

    @Test
    @DisplayName("Should successfully provision a new tenant when TaxID is unique")
    void shouldCreateTenantSuccessfully() {
        // Given
        var profile = new TenantProfile(
                "123456789",
                "contact@thinklab.com",
                "BR",
                "Sao Paulo",
                "America/Sao_Paulo",
                "pt",
                "TECHNOLOGY"
        );
        var command = new CreateTenantCommand("Thinklab Corp", null, profile, "admin-user");

        when(tenantRepository.existsByTaxId(profile.taxId())).thenReturn(Mono.just(false));
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(auditRepository.save(any(TenantAudit.class))).thenReturn(Mono.just(mock(TenantAudit.class)));

        // When
        var result = interactor.execute(command);

        // Then
        StepVerifier.create(result)
                .assertNext(tenant -> {
                    assertEquals("Thinklab Corp", tenant.name());
                })
                .verifyComplete();

        verify(tenantRepository, times(1)).existsByTaxId(profile.taxId());
        verify(tenantRepository, times(1)).save(any(Tenant.class));
        verify(auditRepository, times(1)).save(any(TenantAudit.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when TaxID already exists")
    void shouldThrowExceptionWhenTaxIdExists() {
        // Given
        var profile = new TenantProfile(
                "DUPLICATE_ID",
                "duplicate@thinklab.com",
                "BR",
                "Sao Paulo",
                "America/Sao_Paulo",
                "pt",
                "TECHNOLOGY"
        );
        var command = new CreateTenantCommand("Duplicate Corp", null, profile, "admin-user");

        when(tenantRepository.existsByTaxId(profile.taxId())).thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(interactor.execute(command))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException
                        && ((BusinessException) throwable).getErrorCode().equals("TENANT_DUPLICATE")) // Corrigido aqui para getErrorCode()
                .verify();

        verify(tenantRepository, never()).save(any(Tenant.class));
        verify(auditRepository, never()).save(any(TenantAudit.class));
    }

    @Test
    @DisplayName("Should throw NullPointerException when command is null")
    void shouldThrowExceptionWhenCommandIsNull() {
        assertThrows(NullPointerException.class, () -> interactor.execute(null));
        verifyNoInteractions(tenantRepository, auditRepository);
    }
}