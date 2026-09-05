package com.stockcheck.backend.auth;

import com.stockcheck.backend.auth.dto.AuthResponse;
import com.stockcheck.backend.auth.dto.LoginRequest;
import com.stockcheck.backend.auth.dto.UserResponse;
import com.stockcheck.backend.role.RoleName;
import com.stockcheck.backend.role.UserRole;
import com.stockcheck.backend.role.UserRoleRepository;
import com.stockcheck.backend.security.JwtService;
import com.stockcheck.backend.security.SecurityUtils;
import com.stockcheck.backend.security.StockcheckPrincipal;
import com.stockcheck.backend.tenant.TenantStatus;
import com.stockcheck.backend.user.User;
import com.stockcheck.backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername().trim())
                .orElseThrow(() -> new BadCredentialsException("Неверный логин или пароль"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Неверный логин или пароль");
        }

        if (!user.isActive()) {
            throw new DisabledException("Учетная запись отключена");
        }

        if (user.getTenant() != null && user.getTenant().getStatus() != TenantStatus.ACTIVE) {
            throw new DisabledException("Бизнес заблокирован");
        }

        List<RoleName> roles = userRoleRepository.findByUserId(user.getId()).stream()
                .map(UserRole::getRole)
                .toList();

        StockcheckPrincipal principal = StockcheckPrincipal.fromUser(user, roles);
        String token = jwtService.generateToken(principal);

        return new AuthResponse(
                token,
                jwtService.getExpirationMs(),
                UserResponse.fromUser(user, roles)
        );
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {
        StockcheckPrincipal principal = SecurityUtils.getCurrentPrincipal()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated"));

        return UserResponse.fromPrincipal(principal);
    }
}
