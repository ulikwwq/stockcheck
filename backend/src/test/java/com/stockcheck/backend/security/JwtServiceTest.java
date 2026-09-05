package com.stockcheck.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        properties.setExpirationMs(3600000L); // 1 hour
        jwtService = new JwtService(properties);
    }

    @Test
    @DisplayName("generateToken generates valid token with proper claims")
    void shouldGenerateAndValidateToken() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        StockcheckPrincipal principal = new StockcheckPrincipal(
                userId,
                tenantId,
                "+79991234567",
                "Ivan",
                "Petrov",
                "hashedPass",
                true,
                List.of("ADMINISTRATOR", "ACCOUNTANT")
        );

        String token = jwtService.generateToken(principal);

        assertThat(token).isNotBlank();
        assertThat(jwtService.validateToken(token)).isTrue();

        StockcheckPrincipal extracted = jwtService.extractPrincipal(token);
        assertThat(extracted.getUserId()).isEqualTo(userId);
        assertThat(extracted.getTenantId()).isEqualTo(tenantId);
        assertThat(extracted.getUsername()).isEqualTo("+79991234567");
        assertThat(extracted.getFirstName()).isEqualTo("Ivan");
        assertThat(extracted.getLastName()).isEqualTo("Petrov");
        assertThat(extracted.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_ADMINISTRATOR", "ROLE_ACCOUNTANT");
    }

    @Test
    @DisplayName("validateToken returns false for malformed or tampered token")
    void shouldRejectInvalidToken() {
        assertThat(jwtService.validateToken("invalid.jwt.token")).isFalse();
        assertThat(jwtService.validateToken("")).isFalse();
    }
}
