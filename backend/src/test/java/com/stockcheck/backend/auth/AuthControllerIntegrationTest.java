package com.stockcheck.backend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockcheck.backend.auth.dto.AuthResponse;
import com.stockcheck.backend.auth.dto.LoginRequest;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
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

@WebMvcTest(controllers = AuthController.class)
@Import({
        SecurityConfig.class,
        JwtProperties.class,
        JwtService.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/v1/auth/login returns 200 and token on valid credentials")
    void shouldLoginSuccessfully() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UserResponse userResponse = new UserResponse(
                userId, tenantId, "anna_ivanova", "Anna", "Ivanova", true, List.of(RoleName.ADMINISTRATOR)
        );
        AuthResponse authResponse = new AuthResponse("mock.jwt.token", 86400000L, userResponse);

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        LoginRequest request = new LoginRequest("anna_ivanova", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock.jwt.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("anna_ivanova"))
                .andExpect(jsonPath("$.user.roles[0]").value("ADMINISTRATOR"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login returns 401 on invalid credentials")
    void shouldReturn401OnBadCredentials() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Неверный логин или пароль"));

        LoginRequest request = new LoginRequest("anna_ivanova", "wrongPassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Неверный логин или пароль"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me returns 401 when unauthenticated")
    void shouldReturn401WhenAccessingMeWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me returns user profile when authenticated with Bearer token")
    void shouldReturnProfileWhenAuthenticated() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        StockcheckPrincipal principal = new StockcheckPrincipal(
                userId, tenantId, "anna_ivanova", "Anna", "Ivanova", "", true, List.of("ADMINISTRATOR")
        );
        String token = jwtService.generateToken(principal);

        UserResponse userResponse = new UserResponse(
                userId, tenantId, "anna_ivanova", "Anna", "Ivanova", true, List.of(RoleName.ADMINISTRATOR)
        );
        when(authService.getCurrentUserProfile()).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("anna_ivanova"))
                .andExpect(jsonPath("$.roles[0]").value("ADMINISTRATOR"));
    }
}
