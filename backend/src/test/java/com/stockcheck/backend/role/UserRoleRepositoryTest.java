package com.stockcheck.backend.role;

import com.stockcheck.backend.tenant.Tenant;
import com.stockcheck.backend.tenant.TenantRepository;
import com.stockcheck.backend.user.User;
import com.stockcheck.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRoleRepositoryTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Test
    void aRoleCanBeAssignedToAUser() {
        Tenant tenant = tenantRepository.saveAndFlush(new Tenant("Azamat Store"));
        User user = userRepository.saveAndFlush(
                new User(tenant, "azamat_owner", "hash", "Azamat", "Owner")
        );

        UserRole assignment = userRoleRepository.saveAndFlush(new UserRole(user, RoleName.ADMINISTRATOR));

        assertNotNull(assignment.getId());
        assertNotNull(assignment.getCreatedAt());

        List<UserRole> rolesOfUser = userRoleRepository.findByUserId(user.getId());
        assertEquals(1, rolesOfUser.size());
        assertEquals(RoleName.ADMINISTRATOR, rolesOfUser.get(0).getRole());
    }

    @Test
    void aUserCanHoldMultipleDistinctRoles() {
        Tenant tenant = tenantRepository.saveAndFlush(new Tenant("Nurbek Store"));
        User user = userRepository.saveAndFlush(
                new User(tenant, "nurbek_manager", "hash", "Nurbek", "Manager")
        );

        userRoleRepository.saveAndFlush(new UserRole(user, RoleName.MANAGER));
        userRoleRepository.saveAndFlush(new UserRole(user, RoleName.ACCOUNTANT));

        List<UserRole> rolesOfUser = userRoleRepository.findByUserId(user.getId());
        assertEquals(2, rolesOfUser.size());
    }

    @Test
    void theSameRoleCannotBeAssignedTwiceToTheSameUser() {
        Tenant tenant = tenantRepository.saveAndFlush(new Tenant("Bakyt Store"));
        User user = userRepository.saveAndFlush(
                new User(tenant, "bakyt_seller", "hash", "Bakyt", "Seller")
        );

        userRoleRepository.saveAndFlush(new UserRole(user, RoleName.SELLER));
        UserRole duplicate = new UserRole(user, RoleName.SELLER);

        assertThrows(DataIntegrityViolationException.class, () -> userRoleRepository.saveAndFlush(duplicate));
    }
}
