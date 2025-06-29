package com.listify.backend.controller;

import com.listify.backend.dto.AuthRequest;
import com.listify.backend.dto.AuthResponse;
import com.listify.backend.dto.RegisterRequest;
import com.listify.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling user authentication and registration requests.
 * <p>
 * This class exposes public endpoints under the {@code /api/auth} path
 * for user registration and login.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    /**
     * The service responsible for handling the business logic of authentication.
     * Injected via constructor by Lombok's {@code @RequiredArgsConstructor}.
     */
    private final AuthService authService;

    /**
     * Registers a new user in the system.
     * <p>
     * This endpoint accepts a {@link RegisterRequest} containing the user's details.
     * The request body is validated before processing. On successful registration,
     * it returns an authentication response containing a JWT and user information.
     *
     * @param request the registration request object, validated via {@code @Valid}.
     *                It must contain details like username, email, and password.
     * @return a {@link ResponseEntity} containing the {@link AuthResponse} with a JWT.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Authenticates an existing user and provides a JWT upon successful login.
     * <p>
     * This endpoint accepts an {@link AuthRequest} with the user's credentials (email and password).
     * If the credentials are valid, it generates and returns an authentication response.
     *
     * @param request the login request object containing the user's email and password.
     * @return a {@link ResponseEntity} containing the {@link AuthResponse} with a new JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}