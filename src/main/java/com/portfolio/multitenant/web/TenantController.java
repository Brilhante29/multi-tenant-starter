package com.portfolio.multitenant.web;

import com.portfolio.multitenant.application.TenantDataService;
import com.portfolio.multitenant.application.TenantService;
import com.portfolio.multitenant.domain.Tenant;
import com.portfolio.multitenant.domain.TenantRecord;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenants;
    private final TenantDataService records;

    public TenantController(TenantService tenants, TenantDataService records) {
        this.tenants = tenants;
        this.records = records;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Tenant create(@RequestBody CreateTenantRequest request) {
        return tenants.createTenant(request.name());
    }

    @GetMapping
    List<Tenant> list() {
        return tenants.listTenants();
    }

    @GetMapping("/{tenantId}")
    Tenant get(@PathVariable String tenantId) {
        return tenants.getTenant(tenantId);
    }

    @DeleteMapping("/{tenantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable String tenantId) {
        tenants.deleteTenant(tenantId);
    }

    @PostMapping("/{tenantId}/records")
    @ResponseStatus(HttpStatus.CREATED)
    TenantRecord append(@PathVariable String tenantId, @RequestBody AppendRecordRequest request) {
        return records.append(tenantId, request.payload());
    }

    @GetMapping("/{tenantId}/records")
    List<TenantRecord> listRecords(@PathVariable String tenantId) {
        return records.list(tenantId);
    }

    record CreateTenantRequest(String name) {}

    record AppendRecordRequest(String payload) {}
}
