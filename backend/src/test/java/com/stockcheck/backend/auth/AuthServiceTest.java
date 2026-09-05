package com.stockcheck.backend.auth;

import com.stockcheck.backend.auth.dto.AuthResponse;
import com.stockcheck.backend.auth.dto.LoginRequest;
import com.stockcheck.backend.auth.dto.UserResponse;
import com.stockcheck.backend.role.RoleName;
import com.stockcheck.backend.role.UserRole;
import com.stockcheck.backend.role.UserRoleRepository;
import com.stockcheck.backend.security.JwtService;
import com.stockcheck.backend.security.StockcheckPrincipal;
import com.stockcheck.backend.tenant.Tenant;
import com.stockcheck.backend.tenant.TenantStatus;
import com.stockcheck.backend.user.User;
import com.stockcheck.backend.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Tenant tenant;
    private User user;

    @BeforeEach
    void setUp() {
        tenant = new Tenant("Test Company");
        ReflectionTestUtils.setField(tenant, "id", UUID.randomUUID());

        user = new User(tenant, "shop_owner", "encodedPassword", "Ivan", "Petrov");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("login succeeds with valid username and password")
    void shouldLoginSuccessfully() {
        when(userRepository.findByUsername("shop_owner")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "encodedPassword")).thenReturn(true);
        when(userRoleRepository.findByUserId(user.getId()))
                .thenReturn(List.of(new UserRole(user, RoleName.ADMINISTRATOR)));
        when(jwtService.generateToken(any(StockcheckPrincipal.class))).thenReturn("mock.jwt.token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        LoginRequest request = new LoginRequest("shop_owner", "secret");
        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getUser().getUsername()).isEqualTo("shop_owner");
        assertThat(response.getUser().getRoles()).containsExactly(RoleName.ADMINISTRATOR);
    }

    @Test
    @DisplayName("login throws BadCredentialsException when username is unknown")
    void shouldFailWhenUsernameUnknown() {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("nobody", "secret");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Неверный логин или пароль");
    }

    @Test
    @DisplayName("login throws BadCredentialsException when password does not match")
    void shouldFailWhenPasswordDoesNotMatch() {
        when(userRepository.findByUsername("shop_owner")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        LoginRequest request = new LoginRequest("shop_owner", "wrongPassword");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Неверный логин или пароль");
    }

    @Test
    @DisplayName("login throws DisabledException when user is inactive")
    void shouldFailWhenUserIsInactive() {
        user.setActive(false);
        when(userRepository.findByUsername("shop_owner")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "encodedPassword")).thenReturn(true);

        LoginRequest request = new LoginRequest("shop_owner", "secret");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(DisabledException.class);
    }

    @Test
    @DisplayName("login throws DisabledException when the owning business is deactivated")
    void shouldFailWhenTenantIsInactive() {
        tenant.setStatus(TenantStatus.INACTIVE);
        when(userRepository.findByUsername("shop_owner")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "encodedPassword")).thenReturn(true);

        LoginRequest request = new LoginRequest("shop_owner", "secret");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(DisabledException.class)
                .hasMessage("Бизнес заблокирован");
    }

    @Test
    @DisplayName("getCurrentUserProfile returns user details from SecurityContext")
    void shouldReturnProfileFromSecurityContext() {
        StockcheckPrincipal principal = new StockcheckPrincipal(
                user.getId(),
                tenant.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                "",
                true,
                List.of("ADMINISTRATOR")
        );

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserResponse profile = authService.getCurrentUserProfile();

        assertThat(profile.getId()).isEqualTo(user.getId());
        assertThat(profile.getTenantId()).isEqualTo(tenant.getId());
        assertThat(profile.getUsername()).isEqualTo(user.getUsername());
        assertThat(profile.getRoles()).containsExactly(RoleName.ADMINISTRATOR);
    }
}
