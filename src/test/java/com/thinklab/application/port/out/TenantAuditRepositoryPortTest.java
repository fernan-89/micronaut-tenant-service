package com.thinklab.application.port.out;

import com.thinklab.domain.model.TenantAudit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantAuditRepositoryPortTest {

    @Mock
    private TenantAuditRepositoryPort tenantAuditRepositoryPort;

    @Test
    @DisplayName("Should successfully save an audit record")
    void shouldSaveAudit() {
        // Given
        var tenantId = UUID.randomUUID().toString();
        var audit = createDummyAudit(tenantId);
        when(tenantAuditRepositoryPort.save(any(TenantAudit.class))).thenReturn(Mono.just(audit));

        // When
        Mono<TenantAudit> result = tenantAuditRepositoryPort.save(audit);

        // Then
        StepVerifier.create(result)
                .assertNext(saved -> {
                    assertEquals(audit.id(), saved.id());
                    assertEquals(tenantId, saved.tenantId());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should successfully find audit logs by tenant ID")
    void shouldFindAuditByTenantId() {
        // Given
        var tenantId = UUID.randomUUID().toString();
        // Agora o audit criado tem o mesmo tenantId da busca
        var audit = createDummyAudit(tenantId);

        when(tenantAuditRepositoryPort.findByTenantId(tenantId)).thenReturn(Flux.just(audit));

        // When
        Flux<TenantAudit> result = tenantAuditRepositoryPort.findByTenantId(tenantId);

        // Then
        StepVerifier.create(result)
                .assertNext(found -> {
                    assertEquals(tenantId, found.tenantId());
                })
                .verifyComplete();
    }

    // Helper ajustado para aceitar o tenantId
    private TenantAudit createDummyAudit(String tenantId) {
        return new TenantAudit(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                tenantId,
                "OP_TEST",
                "SUCCESS",
                "admin-user",
                Instant.now(),
                Map.of("key", "value")
        );
    }
}