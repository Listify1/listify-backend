package com.listify.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A custom runtime exception used to indicate authentication or authorization failures.
 * <p>
 * This exception is annotated with {@code @ResponseStatus(HttpStatus.UNAUTHORIZED)},
 * which allows Spring's web framework to automatically translate this exception
 * into an HTTP 401 Unauthorized response when it's thrown from a controller
 * and not otherwise handled.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthException extends RuntimeException {

    /**
     * Constructs a new AuthException with the specified detail message.
     *
     * @param message the detail message, which is saved for later retrieval
     *                by the {@link #getMessage()} method.
     */
    public AuthException(String message) {
        super(message);
    }
}