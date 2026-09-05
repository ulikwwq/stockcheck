package com.stockcheck.backend.user;

import com.stockcheck.backend.auth.dto.UserResponse;
import com.stockcheck.backend.user.dto.CreateUserRequest;
import com.stockcheck.backend.user.dto.UpdateUserRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Seller management for the business ADMINISTRATOR. Sellers can never
 * reach this endpoint (backend-enforced via {@code @PreAuthorize}, not
 * just hidden in the UI).
 */
@RestController
@RequestMapping("/api/v1/sellers")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createSeller(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createSeller(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getSellers() {
        return ResponseEntity.ok(userService.getSellersByCurrentTenant());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateSeller(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.updateSeller(id, request));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<UserResponse> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.setSellerActive(id, true));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<UserResponse> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.setSellerActive(id, false));
    }
}
