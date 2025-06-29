package com.listify.backend.security;

import com.listify.backend.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * A utility class for handling JSON Web Token (JWT) operations.
 * This component is responsible for generating new tokens for authenticated users
 * and for parsing incoming tokens to extract claims (like the user ID).
 * It uses a secret key, configured in the application properties, to sign and verify tokens.
 *
 * @author Listify Team
 * @version 1.0
 * @see User
 */
@Component
public class JwtUtil {

    /**
     * The secret key used for signing and verifying JWTs.
     * This value is injected from the application's configuration file (e.g., application.properties).
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Creates and returns the signing key for JWT operations.
     * It converts the configured {@code jwtSecret} string into a JCA-compliant
     * {@link Key} object using the HS256 algorithm.
     *
     * @return The signing key.
     */
    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    /**
     * Generates a new JWT for a given user.
     * The token's subject is set to the user's ID, and it includes the user's email
     * as a custom claim. The token is set to expire in 24 hours (86,400,000 milliseconds).
     *
     * @param user The {@link User} for whom the token is being generated.
     * @return A compact, URL-safe JWT string.
     */
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the user ID from a given JWT.
     * It parses the token using the configured signing key and retrieves the 'subject'
     * claim, which contains the user ID. This method will throw an exception if the token
     * is invalid, expired, or malformed.
     *
     * @param token The JWT string to parse.
     * @return The user ID as a String.
     */
    public String extractUserId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}


