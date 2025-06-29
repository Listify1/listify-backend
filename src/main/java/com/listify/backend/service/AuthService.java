package com.listify.backend.service;

import com.listify.backend.dto.AuthRequest;
import com.listify.backend.dto.AuthResponse;
import com.listify.backend.dto.RegisterRequest;
import com.listify.backend.exception.AuthException; // WICHTIGER IMPORT
import com.listify.backend.model.User;
import com.listify.backend.repository.UserRepository;
import com.listify.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service layer for handling user authentication and registration.
 * This service orchestrates the user sign-up and login processes, interacting
 * with the {@link UserRepository}, {@link PasswordEncoder}, and {@link JwtUtil}.
 *
 * @author Listify Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Registers a new user in the system.
     *
     * @param request A {@link RegisterRequest} DTO containing user details like username, email, and password.
     * @return An {@link AuthResponse} containing a JWT and basic user information upon successful registration.
     * @throws IllegalArgumentException if the email address is already in use.
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Diese E-Mail-Adresse wird bereits verwendet.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Assign a random avatar from DiceBear
        String baseUrl = "https://api.dicebear.com/7.x/personas/svg?seed=";
        String seed = UUID.randomUUID().toString().substring(0, 8);
        user.setAvatarUrl(baseUrl + seed);

        userRepository.save(user);

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getUsername(), user.getAvatarUrl());
    }

    /**
     * Authenticates an existing user and provides a JWT.
     *
     * @param request An {@link AuthRequest} DTO containing the user's email and password.
     * @return An {@link AuthResponse} containing a new JWT and user details if credentials are valid.
     * @throws AuthException if the email is not registered or if the password does not match.
     */
    public AuthResponse login(AuthRequest request) {
        // 1. Check if the user exists
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("Diese E-Mail-Adresse ist nicht registriert."));

        // 2. Compare passwords
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException("Das Passwort ist falsch. Bitte versuche es erneut.");
        }

        // 3. If credentials are correct, generate a token
        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getUsername(), user.getAvatarUrl());
    }

}