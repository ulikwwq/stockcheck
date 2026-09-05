package com.stockcheck.backend.user;

import com.stockcheck.backend.audit.AuditLogRepository;
import com.stockcheck.backend.auth.dto.UserResponse;
import com.stockcheck.backend.role.RoleName;
import com.stockcheck.backend.role.UserRole;
import com.stockcheck.backend.role.UserRoleRepository;
import com.stockcheck.backend.security.StockcheckPrincipal;
import com.stockcheck.backend.tenant.Tenant;
import com.stockcheck.backend.tenant.TenantRepository;
import com.stockcheck.backend.user.dto.CreateUserRequest;
import com.stockcheck.backend.user.dto.UpdateUserRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UUID tenantId;
    private Tenant tenant;
    private UUID adminId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        tenant = new Tenant("Retail Store");
        ReflectionTestUtils.setField(tenant, "id", tenantId);
        adminId = UUID.randomUUID();

        StockcheckPrincipal principal = new StockcheckPrincipal(
                adminId, tenantId, "shop_admin", "Admin", "User", "pass", true, List.of("ADMINISTRATOR")
        );

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        lenient().when(userRepository.findById(adminId)).thenReturn(Optional.empty());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private CreateUserRequest buildCreateRequest() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("seller01");
        request.setPassword("sellerPass");
        request.setFirstName("Elena");
        request.setLastName("Kuznetsova");
        return request;
    }

    @Test
    @DisplayName("createSeller creates a seller for the current tenant with SELLER role only")
    void shouldCreateSellerSuccessfully() {
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(userRepository.existsByUsername("seller01")).thenReturn(false);
        when(passwordEncoder.encode("sellerPass")).thenReturn("encodedSellerPass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            ReflectionTestUtils.setField(u, "id", UUID.randomUUID());
            return u;
        });

        UserResponse response = userService.createSeller(buildCreateRequest());

        assertThat(response.getId()).isNotNull();
        assertThat(response.getUsername()).isEqualTo("seller01");
        assertThat(response.getRoles()).containsExactly(RoleName.SELLER);

        ArgumentCaptor<UserRole> roleCaptor = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleRepository).save(roleCaptor.capture());
        assertThat(roleCaptor.getValue().getRole()).isEqualTo(RoleName.SELLER);
    }

    @Test
    @DisplayName("createSeller throws 409 Conflict when username already exists")
    void shouldFailWhenDuplicateUsername() {
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(userRepository.existsByUsername("seller01")).thenReturn(true);

        assertThatThrownBy(() -> userService.createSeller(buildCreateRequest()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Такой логин уже существует");
    }

    @Test
    @DisplayName("getSellersByCurrentTenant returns only users holding the SELLER role")
    void shouldReturnOnlySellersForCurrentTenant() {
        User seller = new User(tenant, "seller01", "pass", "Elena", "Kuznetsova");
        ReflectionTestUtils.setField(seller, "id", UUID.randomUUID());
        User admin = new User(tenant, "shop_admin", "pass", "Admin", "User");
        ReflectionTestUtils.setField(admin, "id", UUID.randomUUID());

        when(userRepository.findByTenantId(tenantId)).thenReturn(List.of(seller, admin));
        when(userRoleRepository.findByUserId(seller.getId()))
                .thenReturn(List.of(new UserRole(seller, RoleName.SELLER)));
        when(userRoleRepository.findByUserId(admin.getId()))
                .thenReturn(List.of(new UserRole(admin, RoleName.ADMINISTRATOR)));

        List<UserResponse> sellers = userService.getSellersByCurrentTenant();

        assertThat(sellers).hasSize(1);
        assertThat(sellers.get(0).getUsername()).isEqualTo("seller01");
    }

    @Test
    @DisplayName("setSellerActive deactivates a seller belonging to the current tenant")
    void shouldDeactivateSeller() {
        User seller = new User(tenant, "seller01", "pass", "Elena", "Kuznetsova");
        UUID sellerId = UUID.randomUUID();
        ReflectionTestUtils.setField(seller, "id", sellerId);

        when(userRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(userRoleRepository.findByUserId(sellerId)).thenReturn(List.of(new UserRole(seller, RoleName.SELLER)));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.setSellerActive(sellerId, false);

        assertThat(response.isActive()).isFalse();
    }

    @Test
    @DisplayName("updateSeller rejects sellers belonging to a different tenant")
    void shouldRejectCrossTenantSeller() {
        Tenant otherTenant = new Tenant("Other Business");
        ReflectionTestUtils.setField(otherTenant, "id", UUID.randomUUID());
        User foreignSeller = new User(otherTenant, "foreign_seller", "pass", "X", "Y");
        UUID sellerId = UUID.randomUUID();
        ReflectionTestUtils.setField(foreignSeller, "id", sellerId);

        when(userRepository.findById(sellerId)).thenReturn(Optional.of(foreignSeller));

        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("New Name");

        assertThatThrownBy(() -> userService.updateSeller(sellerId, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("does not belong to your business");
    }
}
