package com.listify.backend.security;

import com.listify.backend.model.User;
import com.listify.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;

/**
 * A Spring Security filter that intercepts all incoming HTTP requests to validate JWTs.
 * This filter is responsible for checking the 'Authorization' header for a Bearer token.
 * If a valid token is found, it extracts the user's identity, fetches the user details,
 * and sets the authentication context in {@link SecurityContextHolder}. This process
 * authenticates the user for the duration of the request.
 * It extends {@link OncePerRequestFilter} to ensure it is executed only once per request,
 * even in the case of internal forwards.
 *
 * @author Listify Team
 * @version 1.0
 * @see JwtUtil
 * @see UserRepository
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtil jwtUtil;
    private final UserRepository userRepo;

    /**
     * Performs the JWT validation and authentication logic for each incoming request.
     * The process is as follows:
     *     Extract the 'Authorization' header.
     *     If the header is present and starts with "Bearer ", extract the token.
     *     Use {@link JwtUtil} to validate the token and extract the user ID.
     *     Fetch the user from the database via {@link UserRepository}.
     *     If the user exists, create an {@link UsernamePasswordAuthenticationToken} and set it in the {@link SecurityContextHolder}.
     *     If any part of the process fails (e.g., invalid token, user not found), the request is rejected.
     *     If no token is present, the request proceeds down the filter chain to be handled by other security mechanisms (e.g., for public endpoints).
     *
     * @param req The incoming {@link HttpServletRequest}.
     * @param res The outgoing {@link HttpServletResponse}.
     * @param chain The filter chain to pass the request along.
     * @throws ServletException If a servlet-specific error occurs.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader("Authorization");
        logger.info("Eingehender Authorization Header: {}", header);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            logger.info("JWT Token extrahiert: {}", token);

            try {
                String userId = jwtUtil.extractUserId(token);
                logger.info("UserID aus Token: {}", userId);

                User user = userRepo.findById(Long.parseLong(userId)).orElseThrow(() -> {
                    logger.warn("Kein User mit ID {} gefunden", userId);
                    return new RuntimeException("User not found");
                });

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("SecurityContext gesetzt für Benutzer: {}", user.getEmail());

            } catch (Exception e) {
                logger.warn("Fehler beim Verarbeiten des JWT-Tokens: {}", e.getMessage());
                res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        } else {
            logger.info("Kein gültiger Bearer-Token im Header");
        }

        chain.doFilter(req, res);
    }

}
