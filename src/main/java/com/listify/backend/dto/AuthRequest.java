package com.listify.backend.dto;

import lombok.Data;

/**
 * Represents a request payload for user authentication.
 * <p>
 * This Data Transfer Object (DTO) is used to capture the credentials (email and password)
 * submitted by a user during the login process.
 */
@Data
public class AuthRequest {

    /**
     * The user's email address, used as the primary identifier for login.
     */
    private String email;

    /**
     * The user's plain-text password. This will be compared against the
     * securely stored hash in the database.
     */
    private String password;
}