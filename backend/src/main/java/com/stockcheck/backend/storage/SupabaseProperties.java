package com.stockcheck.backend.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Supabase Storage configuration, following the same binding pattern as
 * {@link com.stockcheck.backend.security.JwtProperties}. The service-role
 * key is a privileged credential: it is read here from an environment
 * variable only and must never be logged or returned in any API response.
 */
@Component
@ConfigurationProperties(prefix = "supabase")
public class SupabaseProperties {

    /** Project URL, e.g. https://xxxx.supabase.co (no trailing slash). */
    private String url = "";

    /** Service-role key. Backend-only - never sent to the frontend. */
    private String serviceRoleKey = "";

    /** Private bucket used for product photos. */
    private String storageBucket = "product-images";

    /** How long a generated signed URL remains valid. */
    private long signedUrlExpirySeconds = 3600L;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        // Tolerate a trailing slash in the env var without failing at runtime.
        this.url = url != null && url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public String getServiceRoleKey() {
        return serviceRoleKey;
    }

    public void setServiceRoleKey(String serviceRoleKey) {
        this.serviceRoleKey = serviceRoleKey;
    }

    public String getStorageBucket() {
        return storageBucket;
    }

    public void setStorageBucket(String storageBucket) {
        this.storageBucket = storageBucket;
    }

    public long getSignedUrlExpirySeconds() {
        return signedUrlExpirySeconds;
    }

    public void setSignedUrlExpirySeconds(long signedUrlExpirySeconds) {
        this.signedUrlExpirySeconds = signedUrlExpirySeconds;
    }
}
