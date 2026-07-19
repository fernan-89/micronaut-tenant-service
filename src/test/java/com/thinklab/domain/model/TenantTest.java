package com.thinklab.domain.model;

import com.thinklab.domain.valueobject.TenantProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TenantTest {

    @Test
    @DisplayName("Should provision a new tenant with ACTIVE status")
    void shouldProvisionTenant() {
        // Given
        var id = UUID.randomUUID();
        var profile = new TenantProfile("123", "test@test.com", "BR", "SP", "UTC", "pt", "TECH");

        // When
        var tenant = Tenant.provision(id, null, "ThinkLab", profile, "admin");

        // Then
        assertNotNull(tenant);
        assertEquals(id, tenant.id());
        assertEquals("ACTIVE", tenant.status().name());
    }

    @Test
    @DisplayName("Should transition from ACTIVE to SUSPENDED status")
    void shouldSuspendTenant() {
        // Given
        var tenant = createActiveTenant();

        // When
        var suspendedTenant = tenant.suspend("admin");

        // Then
        assertEquals("SUSPENDED", suspendedTenant.status().name());
    }

    @Test
    @DisplayName("Should transition from SUSPENDED back to ACTIVE")
    void shouldReactivateTenant() {
        // Given
        var tenant = createActiveTenant().suspend("admin");

        // When
        var reactivatedTenant = tenant.reactivate("admin");

        // Then
        assertEquals("ACTIVE", reactivatedTenant.status().name());
    }

    @Test
    @DisplayName("Should identify as Branch when parentId is present")
    void shouldIdentifyAsBranch() {
        // Given
        var parentId = UUID.randomUUID();
        var tenant = Tenant.provision(UUID.randomUUID(), parentId, "Branch", getMockProfile(), "admin");

        // Then
        assertTrue(tenant.isBranch());
        assertEquals(parentId, tenant.parentId());
    }

    // Helper methods para manter o código limpo
    private Tenant createActiveTenant() {
        return Tenant.provision(UUID.randomUUID(), null, "Holding", getMockProfile(), "admin");
    }

    private TenantProfile getMockProfile() {
        return new TenantProfile("123", "test@test.com", "BR", "SP", "UTC", "pt", "TECH");
    }
}