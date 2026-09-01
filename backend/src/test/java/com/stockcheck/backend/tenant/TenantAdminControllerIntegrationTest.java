package com.stockcheck.backend.tenant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockcheck.backend.common.GlobalExceptionHandler;
import com.stockcheck.backend.config.SecurityConfig;
import com.stockcheck.backend.security.JwtAuthenticationFilter;
import com.stockcheck.backend.security.JwtProperties;
import com.stockcheck.backend.security.JwtService;
import com.stockcheck.backend.security.RestAccessDeniedHandler;
import com.stockcheck.backend.security.RestAuthenticationEntryPoint;
import com.stockcheck.backend.security.StockcheckPrincipal;
import com.stockcheck.backend.tenant.dto.ChangeUsernameRequest;
import com.stockcheck.backend.tenant.dto.CreateTenantRequest;
import com.stockcheck.backend.tenant.dto.TenantResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TenantAdminController.class)
@Import({
        SecurityConfig.class,
        JwtProperties.class,
        JwtService.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class TenantAdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private TenantService tenantService;

    private CreateTenantRequest buildRequest() {
        CreateTenantRequest request = new CreateTenantRequest();
        request.setTenantName("Retail Network");
        request.setShopName("Store 1");
        request.setOwnerUsername("ivan_ivanov");
        request.setOwnerPassword("ownerPassword");
        request.setOwnerFirstName("Ivan");
        request.setOwnerLastName("Ivanov");
        return request;
    }

    private String superAdminToken() {
        StockcheckPrincipal adminPrincipal = new StockcheckPrincipal(
                UUID.randomUUID(), null, "superadmin", "Root", "Admin", "", true, List.of("SUPER_ADMIN")
        );
        return jwtService.generateToken(adminPrincipal);
    }

    @Test
    @DisplayName("POST /api/v1/admin/tenants creates tenant when user has SUPER_ADMIN role")
    void shouldCreateTenantWhenSuperAdmin() throws Exception {
        String token = superAdminToken();

        UUID tenantId = UUID.randomUUID();
        TenantResponse response = new TenantResponse();
        response.setId(tenantId);
        response.setName("Retail Network");
        response.setStatus(TenantStatus.ACTIVE);
        when(tenantService.createTenant(any(CreateTenantRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/tenants")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(tenantId.toString()))
                .andExpect(jsonPath("$.name").value("Retail Network"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/tenants/{id}/deactivate deactivates a business")
    void shouldDeactivateTenant() throws Exception {
        String token = superAdminToken();
        UUID tenantId = UUID.randomUUID();
        TenantResponse response = new TenantResponse();
        response.setId(tenantId);
        response.setName("Retail Network");
        response.setStatus(TenantStatus.INACTIVE);
        when(tenantService.setStatus(tenantId, TenantStatus.INACTIVE)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/tenants/" + tenantId + "/deactivate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/tenants returns 403 Forbidden when user is ADMINISTRATOR (not SUPER_ADMIN)")
    void shouldReturn403WhenNotSuperAdmin() throws Exception {
        StockcheckPrincipal ownerPrincipal = new StockcheckPrincipal(
                UUID.randomUUID(), UUID.randomUUID(), "shop_owner", "Shop", "Owner", "", true, List.of("ADMINISTRATOR")
        );
        String token = jwtService.generateToken(ownerPrincipal);

        mockMvc.perform(post("/api/v1/admin/tenants")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/tenants returns 401 Unauthorized when not authenticated")
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/admin/tenants"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/tenants/{id} soft-deletes a business when user is SUPER_ADMIN")
    void shouldDeleteTenantWhenSuperAdmin() throws Exception {
        String token = superAdminToken();
        UUID tenantId = UUID.randomUUID();
        TenantResponse response = new TenantResponse();
        response.setId(tenantId);
        response.setName("Retail Network");
        response.setStatus(TenantStatus.DELETED);
        when(tenantService.deleteTenant(tenantId)).thenReturn(response);

        mockMvc.perform(delete("/api/v1/admin/tenants/" + tenantId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELETED"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/tenants/{id} returns 403 Forbidden when user is ADMINISTRATOR (not SUPER_ADMIN)")
    void shouldReturn403WhenNonSuperAdminDeletesTenant() throws Exception {
        StockcheckPrincipal ownerPrincipal = new StockcheckPrincipal(
                UUID.randomUUID(), UUID.randomUUID(), "shop_owner", "Shop", "Owner", "", true, List.of("ADMINISTRATOR")
        );
        String token = jwtService.generateToken(ownerPrincipal);

        mockMvc.perform(delete("/api/v1/admin/tenants/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/tenants/{id}/owner-username changes the owner's username")
    void shouldChangeOwnerUsername() throws Exception {
        String token = superAdminToken();
        UUID tenantId = UUID.randomUUID();
        TenantResponse response = new TenantResponse();
        response.setId(tenantId);
        response.setName("Retail Network");
        response.setStatus(TenantStatus.ACTIVE);
        response.setOwnerUsername("new_login");
        when(tenantService.changeOwnerUsername(any(UUID.class), any(ChangeUsernameRequest.class)))
                .thenReturn(response);

        ChangeUsernameRequest request = new ChangeUsernameRequest();
        request.setNewUsername("new_login");

        mockMvc.perform(patch("/api/v1/admin/tenants/" + tenantId + "/owner-username")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerUsername").value("new_login"));
    }
}
