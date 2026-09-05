package com.stockcheck.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = getSecretKey(jwtProperties.getSecret());
    }

    private SecretKey getSecretKey(String secret) {
        byte[] keyBytes;
        try {
            keyBytes = java.util.HexFormat.of().parseHex(secret);
        } catch (IllegalArgumentException e) {
            try {
                keyBytes = Decoders.BASE64.decode(secret);
            } catch (Exception ex) {
                keyBytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(StockcheckPrincipal principal) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiration = new Date(now + jwtProperties.getExpirationMs());

        List<String> roles = principal.getAuthorities().stream()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .toList();

        return Jwts.builder()
                .subject(principal.getUserId().toString())
                .claim("tenantId", principal.getTenantId() != null ? principal.getTenantId().toString() : null)
                .claim("username", principal.getUsername())
                .claim("firstName", principal.getFirstName())
                .claim("lastName", principal.getLastName())
                .claim("roles", roles)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public StockcheckPrincipal extractPrincipal(String token) {
        Claims claims = extractAllClaims(token);

        UUID userId = UUID.fromString(claims.getSubject());
        String tenantIdStr = claims.get("tenantId", String.class);
        UUID tenantId = (tenantIdStr != null && !tenantIdStr.isBlank()) ? UUID.fromString(tenantIdStr) : null;
        String username = claims.get("username", String.class);
        String firstName = claims.get("firstName", String.class);
        String lastName = claims.get("lastName", String.class);

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        if (roles == null) {
            roles = Collections.emptyList();
        }

        return new StockcheckPrincipal(
                userId,
                tenantId,
                username,
                firstName,
                lastName,
                "",
                true,
                roles
        );
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationMs() {
        return jwtProperties.getExpirationMs();
    }
}
