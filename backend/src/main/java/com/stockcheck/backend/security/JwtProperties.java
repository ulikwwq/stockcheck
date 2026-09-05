package com.stockcheck.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    /**
     * Secret key for HMAC-SHA signing.
     */
    private String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    /**
     * Expiration duration in milliseconds (default 24h = 86400000 ms).
     */
    private long expirationMs = 86400000L;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }
}
