package com.thinklab.application.interactor;

import com.thinklab.application.port.out.TenantAuditRepositoryPort;
import com.thinklab.domain.model.TenantAudit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetTenantAuditLogsInteractorTest {

    @Mock
    private TenantAuditRepositoryPort auditRepository;

    private GetTenantAuditLogsInteractor interactor;

    @BeforeEach
    void setUp() {
        interactor = new GetTenantAuditLogsInteractor(auditRepository);
    }

    @Test
    @DisplayName("Should successfully retrieve audit logs for a given tenant")
    void shouldRetrieveAuditLogsSuccessfully() {
        // Given
        var tenantId = UUID.randomUUID().toString();
        var auditEntry = mock(TenantAudit.class);

        when(auditRepository.findByTenantId(tenantId)).thenReturn(Flux.just(auditEntry));

        // When
        var result = interactor.execute(tenantId);

        // Then
        StepVerifier.create(result)
                .expectNext(auditEntry)
                .verifyComplete();

        verify(auditRepository, times(1)).findByTenantId(tenantId);
    }

    @Test
    @DisplayName("Should return empty flux when no audit logs are found")
    void shouldReturnEmptyFluxWhenNoLogsFound() {
        // Given
        var tenantId = UUID.randomUUID().toString();
        when(auditRepository.findByTenantId(tenantId)).thenReturn(Flux.empty());

        // When
        var result = interactor.execute(tenantId);

        // Then
        StepVerifier.create(result)
                .verifyComplete(); // Verifica que o fluxo termina normalmente sem emitir itens

        verify(auditRepository, times(1)).findByTenantId(tenantId);
    }

    @Test
    @DisplayName("Should throw NullPointerException when tenantId is null")
    void shouldThrowExceptionWhenTenantIdIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> interactor.execute(null));

        // Verifica que o repositório nunca foi acessado
        verifyNoInteractions(auditRepository);
    }
}