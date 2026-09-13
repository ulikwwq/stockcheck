package com.stockcheck.backend.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Thin wrapper around Supabase Storage's REST API.
 *
 * <p>The bucket is private; the service-role key (read from
 * {@link SupabaseProperties}, i.e. an environment variable) is the only
 * credential that can read or write it, and it never leaves this class -
 * not in a log line, not in an API response. The frontend only ever
 * receives short-lived signed URLs generated here.
 *
 * <p>Every method takes a fully-formed storage {@code path}. Callers
 * (see {@code ProductImageService}) are responsible for building that path
 * from server-resolved tenant/product IDs only - this class does not know
 * or care what a "tenant" is, so it cannot be the place a tenant-isolation
 * bug would hide.
 */
@Service
public class SupabaseStorageService {

    private static final Logger log = LoggerFactory.getLogger(SupabaseStorageService.class);

    private final RestClient restClient;
    private final SupabaseProperties properties;
    private final ObjectMapper objectMapper;

    public SupabaseStorageService(SupabaseProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    /** Uploads (or overwrites) the object at {@code path}. */
    public void upload(String path, byte[] content, String contentType) {
        requireConfigured();
        try {
            restClient.put()
                    .uri(objectUri("/storage/v1/object/", path))
                    .headers(h -> authHeaders(h, contentType))
                    .header("x-upsert", "true")
                    .body(content)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error("Supabase Storage upload failed for path {}", path, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Не удалось загрузить изображение. Попробуйте позже.");
        }
    }

    /** Deletes the object at {@code path}. Treated as best-effort by callers. */
    public void delete(String path) {
        requireConfigured();
        try {
            restClient.delete()
                    .uri(objectUri("/storage/v1/object/", path))
                    .headers(this::authHeaders)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            // Best-effort: a stray orphaned blob is far less harmful than
            // failing the caller's request over a cleanup step. Logged so
            // it can be cleaned up manually if it keeps happening.
            log.warn("Supabase Storage delete failed for path {} (continuing)", path, e);
        }
    }

    /** Generates a time-limited signed URL for reading a private object. */
    public String createSignedUrl(String path) {
        requireConfigured();
        try {
            String responseBody = restClient.post()
                    .uri(objectUri("/storage/v1/object/sign/", path))
                    .headers(h -> authHeaders(h, MediaType.APPLICATION_JSON_VALUE))
                    .body(Map.of("expiresIn", properties.getSignedUrlExpirySeconds()))
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(responseBody);
            String signedPath = node.path("signedURL").asText(null);
            if (signedPath == null) {
                throw new IllegalStateException("Supabase response had no signedURL field");
            }
            return properties.getUrl() + "/storage/v1" + signedPath;
        } catch (RestClientException | com.fasterxml.jackson.core.JsonProcessingException | IllegalStateException e) {
            log.error("Supabase Storage sign failed for path {}", path, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Не удалось получить изображение. Попробуйте позже.");
        }
    }

    /**
     * Builds the full object URI by hand rather than via RestClient's
     * {@code uri(template, vars)} template expansion: that expansion
     * percent-encodes each variable as a single opaque segment, which would
     * turn the "/" separators inside our multi-segment storage path (e.g.
     * {@code tenants/<id>/products/<id>/<uuid>.jpg}) into "%2F" and break
     * routing on Supabase's side. UriUtils.encodePath encodes unsafe
     * characters per segment while leaving "/" itself intact.
     */
    private URI objectUri(String apiPrefix, String path) {
        String encodedBucket = UriUtils.encodePathSegment(
                properties.getStorageBucket(),
                StandardCharsets.UTF_8
        );

        String encodedPath = java.util.Arrays.stream(path.split("/"))
                .map(segment -> UriUtils.encodePathSegment(segment, StandardCharsets.UTF_8))
                .collect(java.util.stream.Collectors.joining("/"));

        URI uri = URI.create(
                properties.getUrl()
                        + apiPrefix
                        + encodedBucket
                        + "/"
                        + encodedPath
        );

        log.info("Supabase Storage URI: {}", uri);

        return uri;
    }

    private void authHeaders(HttpHeaders headers, String contentType) {
        headers.set("apikey", properties.getServiceRoleKey());
        headers.setBearerAuth(properties.getServiceRoleKey());
        if (contentType != null) {
            headers.set(HttpHeaders.CONTENT_TYPE, contentType);
        }
    }

    private void authHeaders(HttpHeaders headers) {
        authHeaders(headers, null);
    }

    private void requireConfigured() {
        if (properties.getUrl().isBlank() || properties.getServiceRoleKey().isBlank()) {
            log.error("Supabase Storage is not configured (SUPABASE_URL / SUPABASE_SERVICE_ROLE_KEY missing)");
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Хранилище изображений недоступно");
        }
    }
}
