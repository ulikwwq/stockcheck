package com.stockcheck.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    List<User> findByTenantId(UUID tenantId);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<User> findByTenantIdAndUsername(UUID tenantId, String username);
}
