package com.listify.backend.dto;

/**
 * Represents the response payload sent to a client after a successful authentication or registration.
 * <p>
 * This Data Transfer Object (DTO) encapsulates the essential information the client needs
 * to maintain an authenticated session, including a JSON Web Token (JWT) and basic user details.
 */
public class AuthResponse {

    /**
     * The JSON Web Token (JWT) generated for the authenticated user.
     * The client should include this token in the 'Authorization' header of subsequent requests.
     */
    public String token;

    /**
     * The unique identifier of the authenticated user.
     */
    public Long userId;

    /**
     * The email address of the authenticated user.
     */
    public String email;

    /**
     * The username of the authenticated user.
     */
    public String username;

    /**
     * The URL pointing to the user's avatar image.
     */
    public String avatarUrl;

    /**
     * Constructs a new AuthResponse.
     *
     * @param token     The JWT for the session.
     * @param userId    The unique ID of the user.
     * @param email     The email address of the user.
     * @param username  The username of the user.
     * @param avatarUrl The URL of the user's avatar.
     */
    public AuthResponse(String token, Long userId, String email, String username, String avatarUrl) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.avatarUrl = avatarUrl;
    }
}
