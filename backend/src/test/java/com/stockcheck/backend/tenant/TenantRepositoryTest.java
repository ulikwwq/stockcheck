package com.stockcheck.backend.tenant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs against the real PostgreSQL instance configured in application.yaml
 * (the same one started by {@code docker compose up -d}), not an embedded
 * database - this exercises the actual Flyway migration and Postgres-
 * specific behavior (UUID generation, constraints).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TenantRepositoryTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Test
    void persistsAndReloadsATenant() {
        Tenant saved = tenantRepository.saveAndFlush(new Tenant("Azamat Store"));

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());

        Optional<Tenant> reloaded = tenantRepository.findById(saved.getId());

        assertTrue(reloaded.isPresent());
        assertEquals("Azamat Store", reloaded.get().getName());
    }

    @Test
    void generatesADistinctIdForEachTenant() {
        Tenant first = tenantRepository.saveAndFlush(new Tenant("Tenant A"));
        Tenant second = tenantRepository.saveAndFlush(new Tenant("Tenant B"));

        assertNotEquals(first.getId(), second.getId());
    }
}
