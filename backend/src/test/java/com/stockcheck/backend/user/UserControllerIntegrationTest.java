package com.stockcheck.backend.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockcheck.backend.auth.dto.UserResponse;
import com.stockcheck.backend.common.GlobalExceptionHandler;
import com.stockcheck.backend.config.SecurityConfig;
import com.stockcheck.backend.role.RoleName;
import com.stockcheck.backend.security.JwtAuthenticationFilter;
import com.stockcheck.backend.security.JwtProperties;
import com.stockcheck.backend.security.JwtService;
import com.stockcheck.backend.security.RestAccessDeniedHandler;
import com.stockcheck.backend.security.RestAuthenticationEntryPoint;
import com.stockcheck.backend.security.StockcheckPrincipal;
import com.stockcheck.backend.user.dto.CreateUserRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import({
        SecurityConfig.class,
        JwtProperties.class,
        JwtService.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    private CreateUserRequest buildRequest() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("seller01");
        request.setPassword("sellerPassword");
        request.setFirstName("Anna");
        request.setLastName("Seller");
        return request;
    }

    @Test
    @DisplayName("POST /api/v1/sellers creates seller when user is ADMINISTRATOR")
    void shouldCreateSellerWhenAdministrator() throws Exception {
        UUID tenantId = UUID.randomUUID();
        StockcheckPrincipal adminPrincipal = new StockcheckPrincipal(
                UUID.randomUUID(), tenantId, "shop_admin", "Admin", "User", "", true, List.of("ADMINISTRATOR")
        );
        String token = jwtService.generateToken(adminPrincipal);

        UUID sellerId = UUID.randomUUID();
        UserResponse response = new UserResponse(
                sellerId, tenantId, "seller01", "Anna", "Seller", true, List.of(RoleName.SELLER)
        );
        when(userService.createSeller(any(CreateUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/sellers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(sellerId.toString()))
                .andExpect(jsonPath("$.username").value("seller01"))
                .andExpect(jsonPath("$.roles[0]").value("SELLER"));
    }

    @Test
    @DisplayName("POST /api/v1/sellers returns 403 Forbidden when user is SELLER")
    void shouldReturn403WhenSellerTriesToCreateUser() throws Exception {
        UUID tenantId = UUID.randomUUID();
        StockcheckPrincipal sellerPrincipal = new StockcheckPrincipal(
                UUID.randomUUID(), tenantId, "seller01", "Anna", "Seller", "", true, List.of("SELLER")
        );
        String token = jwtService.generateToken(sellerPrincipal);

        mockMvc.perform(post("/api/v1/sellers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("GET /api/v1/sellers returns sellers for ADMINISTRATOR")
    void shouldReturnSellersForAdmin() throws Exception {
        UUID tenantId = UUID.randomUUID();
        StockcheckPrincipal adminPrincipal = new StockcheckPrincipal(
                UUID.randomUUID(), tenantId, "shop_admin", "Admin", "User", "", true, List.of("ADMINISTRATOR")
        );
        String token = jwtService.generateToken(adminPrincipal);

        UserResponse seller = new UserResponse(
                UUID.randomUUID(), tenantId, "seller01", "Anna", "Seller", true, List.of(RoleName.SELLER)
        );
        when(userService.getSellersByCurrentTenant()).thenReturn(List.of(seller));

        mockMvc.perform(get("/api/v1/sellers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("seller01"));
    }
}
