package com.listify.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A global exception handler for the entire application.
 * <p>
 * This class uses {@code @ControllerAdvice} to centralize exception handling logic.
 * It catches specific exceptions thrown by controllers and translates them into
 * consistent, user-friendly HTTP responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles authentication-related exceptions (e.g., wrong password, user not found).
     *
     * @param ex The caught {@link AuthException}.
     * @return A {@link ResponseEntity} with a 401 Unauthorized status and an error message.
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, String>> handleAuthException(AuthException ex) {
        return new ResponseEntity<>(Map.of("message", ex.getMessage()), HttpStatus.UNAUTHORIZED); // 401
    }

    /**
     * Handles general illegal argument exceptions, often used for business logic validation
     * during registration (e.g., email already exists).
     *
     * @param ex The caught {@link IllegalArgumentException}.
     * @return A {@link ResponseEntity} with a 400 Bad Request status and an error message.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>(Map.of("message", ex.getMessage()), HttpStatus.BAD_REQUEST); // 400
    }

    /**
     * Handles validation errors from DTOs annotated with {@code @Valid}.
     * <p>
     * This method is triggered when bean validation constraints (e.g., {@code @NotBlank}, {@code @Size})
     * on a request body fail. It collects all default error messages into a list.
     *
     * @param ex The caught {@link MethodArgumentNotValidException}.
     * @return A {@link ResponseEntity} with a 400 Bad Request status and a map containing a list of validation error messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.toList());
        return new ResponseEntity<>(Map.of("errors", errors), HttpStatus.BAD_REQUEST); // 400
    }
}