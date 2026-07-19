package com.thinklab.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TenantAuditTest {

    @Test
    @DisplayName("Should create a valid Root Audit entry")
    void shouldCreateRootAudit() {
        // Given
        var tenantId = UUID.randomUUID().toString();
        var operation = "ROOT_PROVISIONING";
        var executor = "admin";

        // When
        var audit = TenantAudit.createRootAudit(tenantId, operation, executor, null);

        // Then
        assertNotNull(audit);
        assertEquals(tenantId, audit.tenantId());
        assertEquals(operation, audit.operation());
        assertEquals(executor, audit.executorId());
    }

    @Test
    @DisplayName("Should create a valid Branch Audit entry with parent reference")
    void shouldCreateBranchAudit() {
        // Given
        var tenantId = UUID.randomUUID().toString();
        var parentId = UUID.randomUUID().toString();
        var operation = "BRANCH_PROVISIONING";
        var executor = "system";

        // When
        var audit = TenantAudit.createBranchAudit(tenantId, parentId, operation, executor, null);

        // Then
        assertNotNull(audit);
        assertEquals(tenantId, audit.tenantId());
        assertEquals(operation, audit.operation());
        assertEquals(executor, audit.executorId());

        // Verificando a amarração da hierarquia no metadata do record
        assertNotNull(audit.metadata());
        assertEquals(parentId, audit.metadata().get("parent_id_binding"));
        assertEquals("BRANCH", audit.metadata().get("hierarchy_role"));
    }
}