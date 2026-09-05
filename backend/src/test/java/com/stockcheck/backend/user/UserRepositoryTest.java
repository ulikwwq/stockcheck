package com.stockcheck.backend.user;

import com.stockcheck.backend.tenant.Tenant;
import com.stockcheck.backend.tenant.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void userBelongsToATenant() {
        Tenant tenant = tenantRepository.saveAndFlush(new Tenant("Nurbek Store"));

        User user = userRepository.saveAndFlush(
                new User(tenant, "nurbek_uulu", "hashed-password", "Nurbek", "Uulu")
        );

        assertNotNull(user.getId());
        assertEquals(tenant.getId(), user.getTenant().getId());

        List<User> usersOfTenant = userRepository.findByTenantId(tenant.getId());
        assertEquals(1, usersOfTenant.size());

        Optional<User> byUsername = userRepository.findByTenantIdAndUsername(tenant.getId(), "nurbek_uulu");
        assertTrue(byUsername.isPresent());

        assertTrue(userRepository.existsByUsername("nurbek_uulu"));
        assertFalse(userRepository.existsByUsername("someone_else"));
    }

    @Test
    void duplicateUsernameWithinSameTenantIsRejected() {
        Tenant tenant = tenantRepository.saveAndFlush(new Tenant("Bakyt Store"));

        userRepository.saveAndFlush(
                new User(tenant, "bakyt_bekov", "hashed-password", "Bakyt", "Bekov")
        );

        User duplicate = new User(tenant, "bakyt_bekov", "hashed-password", "Someone", "Else");

        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(duplicate));
    }

    @Test
    void duplicateUsernameAcrossDifferentTenantsIsAlsoRejected() {
        // Username is unique GLOBALLY (not per-tenant), since login has no
        // business selector - two businesses cannot share a username.
        Tenant tenantA = tenantRepository.saveAndFlush(new Tenant("Tenant A"));
        Tenant tenantB = tenantRepository.saveAndFlush(new Tenant("Tenant B"));

        userRepository.saveAndFlush(new User(tenantA, "shared_login", "hash", "A", "Owner"));

        User userB = new User(tenantB, "shared_login", "hash", "B", "Owner");

        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(userB));
    }

    @Test
    void firstAndLastNameAreOptional() {
        Tenant tenant = tenantRepository.saveAndFlush(new Tenant("Minimal Store"));

        User user = userRepository.saveAndFlush(
                new User(tenant, "minimal_owner", "hashed-password", null, null)
        );

        assertNotNull(user.getId());
        assertEquals(null, user.getFirstName());
        assertEquals(null, user.getLastName());
    }
}
