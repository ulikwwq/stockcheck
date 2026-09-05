package com.stockcheck.backend.tenant;

import com.stockcheck.backend.audit.AuditLogRepository;
import com.stockcheck.backend.role.RoleName;
import com.stockcheck.backend.role.UserRole;
import com.stockcheck.backend.role.UserRoleRepository;
import com.stockcheck.backend.shop.Shop;
import com.stockcheck.backend.shop.ShopRepository;
import com.stockcheck.backend.tenant.dto.CreateTenantRequest;
import com.stockcheck.backend.tenant.dto.ResetPasswordRequest;
import com.stockcheck.backend.tenant.dto.TenantResponse;
import com.stockcheck.backend.user.User;
import com.stockcheck.backend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TenantService tenantService;

    private CreateTenantRequest buildRequest() {
        CreateTenantRequest request = new CreateTenantRequest();
        request.setTenantName("Super Market");
        request.setShopName("Flagship Store");
        request.setOwnerUsername("dmitry_owner");
        request.setOwnerPassword("secretPass");
        request.setOwnerFirstName("Dmitry");
        request.setOwnerLastName("Sidorov");
        return request;
    }

    @BeforeEach
    void setUp() {
        lenient().when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> {
            Tenant t = invocation.getArgument(0);
            if (ReflectionTestUtils.getField(t, "id") == null) {
                ReflectionTestUtils.setField(t, "id", UUID.randomUUID());
            }
            return t;
        });
        lenient().when(shopRepository.save(any(Shop.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            if (ReflectionTestUtils.getField(u, "id") == null) {
                ReflectionTestUtils.setField(u, "id", UUID.randomUUID());
            }
            return u;
        });
    }

    @Test
    @DisplayName("createTenant creates Tenant, default Shop, Owner User, and assigns ADMINISTRATOR role")
    void shouldCreateTenantWithShopAndOwner() {
        CreateTenantRequest request = buildRequest();
        when(passwordEncoder.encode("secretPass")).thenReturn("encodedPassword");
        when(userRepository.existsByUsername("dmitry_owner")).thenReturn(false);

        TenantResponse response = tenantService.createTenant(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("Super Market");
        assertThat(response.getStatus()).isEqualTo(TenantStatus.ACTIVE);
        assertThat(response.getOwnerUsername()).isEqualTo("dmitry_owner");

        ArgumentCaptor<Shop> shopCaptor = ArgumentCaptor.forClass(Shop.class);
        verify(shopRepository).save(shopCaptor.capture());
        assertThat(shopCaptor.getValue().getName()).isEqualTo("Flagship Store");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getUsername()).isEqualTo("dmitry_owner");
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("encodedPassword");

        ArgumentCaptor<UserRole> roleCaptor = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleRepository).save(roleCaptor.capture());
        assertThat(roleCaptor.getValue().getRole()).isEqualTo(RoleName.ADMINISTRATOR);
    }

    @Test
    @DisplayName("createTenant rejects a username that is already taken")
    void shouldRejectDuplicateUsername() {
        CreateTenantRequest request = buildRequest();
        when(userRepository.existsByUsername("dmitry_owner")).thenReturn(true);

        assertThatThrownBy(() -> tenantService.createTenant(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Такой логин уже существует");
    }

    @Test
    @DisplayName("setStatus deactivates a business")
    void shouldDeactivateTenant() {
        Tenant tenant = new Tenant("Super Market");
        UUID tenantId = UUID.randomUUID();
        ReflectionTestUtils.setField(tenant, "id", tenantId);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        TenantResponse response = tenantService.setStatus(tenantId, TenantStatus.INACTIVE);

        assertThat(response.getStatus()).isEqualTo(TenantStatus.INACTIVE);
    }

    @Test
    @DisplayName("resetOwnerPassword updates the owner's password hash")
    void shouldResetOwnerPassword() {
        Tenant tenant = new Tenant("Super Market");
        UUID tenantId = UUID.randomUUID();
        ReflectionTestUtils.setField(tenant, "id", tenantId);
        User owner = new User(tenant, "dmitry_owner", "oldHash", "Dmitry", "Sidorov");
        tenant.setOwner(owner);

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(passwordEncoder.encode("newSecret123")).thenReturn("newHash");

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword("newSecret123");

        tenantService.resetOwnerPassword(tenantId, request);

        assertThat(owner.getPasswordHash()).isEqualTo("newHash");
        verify(userRepository).save(owner);
    }

    @Test
    @DisplayName("deleteTenant soft-deletes the business (sets DELETED status) without removing any data")
    void shouldSoftDeleteTenant() {
        Tenant tenant = new Tenant("Super Market");
        UUID tenantId = UUID.randomUUID();
        ReflectionTestUtils.setField(tenant, "id", tenantId);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        TenantResponse response = tenantService.deleteTenant(tenantId);

        assertThat(response.getStatus()).isEqualTo(TenantStatus.DELETED);
        // Soft delete only flips status - the same Tenant row is saved, never removed.
        verify(tenantRepository, org.mockito.Mockito.never()).delete(any(Tenant.class));
        verify(tenantRepository, org.mockito.Mockito.atLeastOnce()).save(tenant);
    }

    @Test
    @DisplayName("changeOwnerUsername updates the username without creating a new user or touching the tenant")
    void shouldChangeOwnerUsername() {
        Tenant tenant = new Tenant("Super Market");
        UUID tenantId = UUID.randomUUID();
        ReflectionTestUtils.setField(tenant, "id", tenantId);
        User owner = new User(tenant, "old_login", "hash", "Dmitry", "Sidorov");
        UUID ownerId = UUID.randomUUID();
        ReflectionTestUtils.setField(owner, "id", ownerId);
        tenant.setOwner(owner);

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(userRepository.existsByUsername("new_login")).thenReturn(false);

        com.stockcheck.backend.tenant.dto.ChangeUsernameRequest request =
                new com.stockcheck.backend.tenant.dto.ChangeUsernameRequest();
        request.setNewUsername("new_login");

        TenantResponse response = tenantService.changeOwnerUsername(tenantId, request);

        assertThat(response.getOwnerUsername()).isEqualTo("new_login");
        assertThat(owner.getId()).isEqualTo(ownerId); // same user, same ID
        assertThat(owner.getTenant()).isSameAs(tenant); // same tenant
        verify(userRepository).save(owner);
    }

    @Test
    @DisplayName("changeOwnerUsername rejects a username already used by someone else")
    void shouldRejectDuplicateUsernameOnChange() {
        Tenant tenant = new Tenant("Super Market");
        UUID tenantId = UUID.randomUUID();
        ReflectionTestUtils.setField(tenant, "id", tenantId);
        User owner = new User(tenant, "old_login", "hash", "Dmitry", "Sidorov");
        tenant.setOwner(owner);

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(userRepository.existsByUsername("taken_login")).thenReturn(true);

        com.stockcheck.backend.tenant.dto.ChangeUsernameRequest request =
                new com.stockcheck.backend.tenant.dto.ChangeUsernameRequest();
        request.setNewUsername("taken_login");

        assertThatThrownBy(() -> tenantService.changeOwnerUsername(tenantId, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Такой логин уже существует");
    }
}
