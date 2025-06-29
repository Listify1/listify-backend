package com.listify.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Represents a request payload for new user registration.
 * <p>
 * This Data Transfer Object (DTO) contains the necessary user details for creating a new account.
 * It includes validation constraints to ensure the integrity and security of the provided data
 * before it is processed by the service layer.
 */
@Data
public class RegisterRequest {

    /**
     * The desired username for the new user. It is subject to size and character constraints.
     */
    @NotBlank(message = "Benutzername darf nicht leer sein")
    @Size(min = 3, max = 20, message = "Benutzername muss zwischen 3 und 20 Zeichen lang sein")
    @Pattern(regexp = "^[a-zA-ZäöüÄÖÜß ]+$", message = "Benutzername darf nur Buchstaben und Leerzeichen enthalten")
    private String username;

    /**
     * The user's email address, which will be used for login and communication.
     * It must be a well-formed email address.
     */
    @NotBlank(message = "E-Mail darf nicht leer sein")
    @Email(message = "Bitte gib eine gültige E-Mail-Adresse ein")
    private String email;

    /**
     * The user's desired password. It must meet minimum length and complexity requirements
     * to ensure account security.
     */
    @NotBlank(message = "Passwort darf nicht leer sein")
    @Size(min = 6, message = "Passwort muss mindestens 6 Zeichen lang sein")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
            message = "Passwort muss mindestens eine Zahl, einen Kleinbuchstaben, einen Großbuchstaben und ein Sonderzeichen (@#$%^&+=!) enthalten")
    private String password;
}