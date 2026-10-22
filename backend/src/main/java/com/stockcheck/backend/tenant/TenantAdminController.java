package com.stockcheck.backend.tenant;

import com.stockcheck.backend.tenant.dto.ChangeUsernameRequest;
import com.stockcheck.backend.tenant.dto.CreateTenantRequest;
import com.stockcheck.backend.tenant.dto.ResetPasswordRequest;
import com.stockcheck.backend.tenant.dto.TenantResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/tenants")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class TenantAdminController {

    private final TenantService tenantService;
    private final TenantPurgeService tenantPurgeService;

    public TenantAdminController(
            TenantService tenantService,
            TenantPurgeService tenantPurgeService
    ) {
        this.tenantService = tenantService;
        this.tenantPurgeService = tenantPurgeService;
    }

    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(
            @Valid @RequestBody CreateTenantRequest request
    ) {
        TenantResponse response = tenantService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TenantResponse>> getAllTenants() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> getTenantById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(tenantService.getTenantById(id));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<TenantResponse> activate(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                tenantService.setStatus(id, TenantStatus.ACTIVE)
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<TenantResponse> deactivate(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                tenantService.setStatus(id, TenantStatus.INACTIVE)
        );
    }

    @PostMapping("/{id}/reset-owner-password")
    public ResponseEntity<Void> resetOwnerPassword(
            @PathVariable UUID id,
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        tenantService.resetOwnerPassword(id, request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/owner-username")
    public ResponseEntity<TenantResponse> changeOwnerUsername(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeUsernameRequest request
    ) {
        return ResponseEntity.ok(
                tenantService.changeOwnerUsername(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(
            @PathVariable UUID id
    ) {
        tenantPurgeService.purge(id);
        return ResponseEntity.noContent().build();
    }
}

