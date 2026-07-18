package com.thinklab.application.port.out;

import com.thinklab.domain.model.TenantAudit;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TenantAuditRepositoryPort {
    Mono<TenantAudit> save(TenantAudit audit);
    Mono<TenantAudit> findById(String id);
    Flux<TenantAudit> findByTxId(String txId);

    // Adicione esta linha:
    Flux<TenantAudit> findByTenantId(String tenantId);
}