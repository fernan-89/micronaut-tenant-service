package com.thinklab.application.interactor;

import com.thinklab.application.port.out.TenantRepositoryPort;
import com.thinklab.application.usecase.query.ListTenantsQuery;
import com.thinklab.domain.model.Tenant;
import io.micronaut.data.model.Pageable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListTenantsInteractorTest {

    @Mock
    private TenantRepositoryPort tenantRepository;

    @Mock
    private ListTenantsQuery query;

    private ListTenantsInteractor interactor;

    @BeforeEach
    void setUp() {
        interactor = new ListTenantsInteractor(tenantRepository);
    }

    @Test
    @DisplayName("Should retrieve root tenants when isRootLookup is true")
    void shouldListRootsSuccessfully() {
        // Given
        var tenant = mock(Tenant.class);
        when(query.page()).thenReturn(0);
        when(query.size()).thenReturn(10);
        when(query.isRootLookup()).thenReturn(true);
        when(tenantRepository.findRoots(any(Pageable.class))).thenReturn(Flux.just(tenant));

        // When
        Flux<Tenant> result = interactor.execute(query);

        // Then
        StepVerifier.create(result)
                .expectNext(tenant)
                .verifyComplete();

        verify(tenantRepository, times(1)).findRoots(any(Pageable.class));
        verify(tenantRepository, never()).findByParentId(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should retrieve branch tenants when isRootLookup is false")
    void shouldListBranchesSuccessfully() {
        // Given
        var tenant = mock(Tenant.class);
        var parentId = "parent-id-123";
        when(query.page()).thenReturn(0);
        when(query.size()).thenReturn(10);
        when(query.isRootLookup()).thenReturn(false);
        when(query.parentId()).thenReturn(parentId);

        when(tenantRepository.findByParentId(eq(parentId), any(Pageable.class)))
                .thenReturn(Flux.just(tenant));

        // When
        Flux<Tenant> result = interactor.execute(query);

        // Then
        StepVerifier.create(result)
                .expectNext(tenant)
                .verifyComplete();

        verify(tenantRepository, times(1)).findByParentId(eq(parentId), any(Pageable.class));
        verify(tenantRepository, never()).findRoots(any(Pageable.class));
    }

    @Test
    @DisplayName("Should throw NullPointerException when query is null")
    void shouldThrowExceptionWhenQueryIsNull() {
        // When / Then
        assertThrows(NullPointerException.class, () -> interactor.execute(null));
    }
}