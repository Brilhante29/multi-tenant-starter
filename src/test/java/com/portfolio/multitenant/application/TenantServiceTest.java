package com.portfolio.multitenant.application;

import com.portfolio.multitenant.domain.Tenant;
import com.portfolio.multitenant.domain.TenantRepository;
import com.portfolio.multitenant.infrastructure.SchemaInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private SchemaInitializer schemaInitializer;

    private TenantService tenantService;

    @BeforeEach
    void setUp() {
        tenantService = new TenantService(tenantRepository, schemaInitializer);
    }

    private Tenant copyOf(Tenant t) {
        return new Tenant(t.getId(), t.getName(), t.getSchema(), t.getStatus(), t.getCreatedAt());
    }

    @Test
    void shouldCreateTenantSuccessfully() {
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(i -> copyOf(i.getArgument(0)));

        Tenant result = tenantService.createTenant("my-tenant");

        assertNotNull(result.getId());
        assertEquals("my-tenant", result.getName());
        assertNotNull(result.getSchema());
        assertTrue(result.getSchema().startsWith("tenant_"));
        assertEquals(Tenant.Status.ACTIVE, result.getStatus());
        assertNotNull(result.getCreatedAt());

        verify(tenantRepository, times(2)).save(any(Tenant.class));
        verify(schemaInitializer).initialize(result.getId());
    }

    @Test
    void shouldStartWithProvisioningStatus() {
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(i -> copyOf(i.getArgument(0)));

        tenantService.createTenant("prov-test");

        ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
        verify(tenantRepository, times(2)).save(captor.capture());
        List<Tenant> savedTenants = captor.getAllValues();
        assertEquals(2, savedTenants.size());
        assertEquals(Tenant.Status.PROVISIONING, savedTenants.get(0).getStatus());
        assertEquals(Tenant.Status.ACTIVE, savedTenants.get(1).getStatus());
    }

    @Test
    void shouldMarkInactiveWhenSchemaInitFails() {
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(i -> copyOf(i.getArgument(0)));
        doThrow(new RuntimeException("migration failed")).when(schemaInitializer).initialize(any());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> tenantService.createTenant("fail-tenant"));
        assertTrue(ex.getMessage().contains("fail-tenant"));

        verify(tenantRepository, times(2)).save(any(Tenant.class));
    }

    @Test
    void shouldListTenants() {
        when(tenantRepository.findAll()).thenReturn(List.of(
            new Tenant("1", "a", "s1", Tenant.Status.ACTIVE, null),
            new Tenant("2", "b", "s2", Tenant.Status.ACTIVE, null)
        ));

        List<Tenant> tenants = tenantService.listTenants();
        assertEquals(2, tenants.size());
    }
}
