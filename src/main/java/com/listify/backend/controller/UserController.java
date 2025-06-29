package com.listify.backend.controller;

import com.listify.backend.dto.DeletionCheckDTO;
import com.listify.backend.model.Group;
import com.listify.backend.model.User;
import com.listify.backend.model.enums.UserPermission;
import com.listify.backend.repository.UserRepository;
import com.listify.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing user-related operations.
 * <p>
 * This class exposes REST endpoints under the {@code /api/users} path for creating,
 * retrieving, updating, and deleting user accounts and their associated data.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * Constructs the controller and injects required dependencies.
     *
     * @param userService    The service for handling user business logic.
     * @param userRepository The repository for direct user data access.
     */
    @Autowired
    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * Checks if the currently authenticated user is allowed to delete their own account.
     * <p>
     * This typically involves checking for unresolved debts or other blocking conditions.
     *
     * @param currentUser The currently authenticated user, injected by Spring Security.
     * @return A {@link ResponseEntity} containing a {@link DeletionCheckDTO} with the status and a message.
     */
    @GetMapping("/check-deletion-status")
    public ResponseEntity<?> checkDeletionStatus(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.checkAccountDeletionStatus(currentUser));
    }

    /**
     * Deletes the account of the currently authenticated user.
     *
     * @param currentUser The currently authenticated user, injected by Spring Security.
     * @return A {@link ResponseEntity} with 204 No Content on successful deletion.
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(@AuthenticationPrincipal User currentUser) {
        userService.deleteUserAccount(currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves a user by their unique ID.
     *
     * @param id The ID of the user to retrieve.
     * @return A {@link ResponseEntity} containing the {@link User} if found, or 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves a user by their email address.
     *
     * @param email The email address of the user to retrieve.
     * @return A {@link ResponseEntity} containing the {@link User} if found, or 404 Not Found.
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getByEmail(@PathVariable String email) {
        return userService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves a list of all users in the system.
     *
     * @return A list of all {@link User} entities.
     */
    @GetMapping
    public List<User> all() {
        return userService.findAll();
    }

    /**
     * Creates a new user.
     *
     * @param u The user object to create.
     * @return The saved {@link User} entity with a 201 Created status.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@RequestBody User u) {
        return userService.save(u);
    }

    /**
     * Deletes a user by their unique ID.
     *
     * @param id The ID of the user to delete.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }

    /**
     * Retrieves the group associated with a specific user.
     *
     * @param id The ID of the user.
     * @return A {@link ResponseEntity} containing the {@link Group} if found, or 404 Not Found.
     */
    @GetMapping("/{id}/group")
    public ResponseEntity<Group> getUserGroup(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(User::getGroup)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves the permission level of a specific user.
     *
     * @param userId The ID of the user.
     * @return A {@link ResponseEntity} containing a map with the permission level (e.g., {"permission": "ADMIN"}).
     */
    @GetMapping("/{userId}/permission")
    public ResponseEntity<Map<String, String>> getUserPermission(@PathVariable Long userId) {
        UserPermission permission = userService.getPermissionByUserId(userId);
        Map<String, String> response = new HashMap<>();
        response.put("permission", permission.name());
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a user's avatar URL.
     *
     * @param id      The ID of the user to update.
     * @param payload A map containing the new avatar URL, e.g., {"avatarUrl": "http://..."}.
     * @return A {@link ResponseEntity} with the updated {@link User} object.
     */
    @PatchMapping("/{id}/avatar")
    public ResponseEntity<User> updateAvatar(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return userService.findById(id).map(user -> {
            user.setAvatarUrl(payload.get("avatarUrl"));
            return ResponseEntity.ok(userService.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates a user's PayPal email address.
     *
     * @param id   The ID of the user to update.
     * @param body A map containing the new PayPal email, e.g., {"paypalEmail": "user@example.com"}.
     * @return The updated {@link User} object.
     */
    @PatchMapping("/{id}/paypal")
    public User updatePaypal(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String paypal = body.get("paypalEmail");
        User user = userService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        user.setPaypalEmail(paypal);
        return userService.save(user);
    }

    /**
     * Updates a user's username.
     *
     * @param id      The ID of the user to update.
     * @param payload A map containing the new username, e.g., {"username": "NewName"}.
     * @return A {@link ResponseEntity} with the updated {@link User} object.
     */
    @PatchMapping("/{id}/username")
    public ResponseEntity<User> updateUsername(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return userService.findById(id).map(user -> {
            user.setUsername(payload.get("username"));
            return ResponseEntity.ok(userService.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Checks if a specific user (by ID) is allowed to be deleted or removed from a group.
     *
     * @param userId The ID of the user to check.
     * @return A {@link ResponseEntity} containing a {@link DeletionCheckDTO} with the status and a message.
     * @throws ResponseStatusException if the user is not found.
     */
    @GetMapping("/{userId}/check-deletion-status")
    public ResponseEntity<DeletionCheckDTO> checkUserDeletionStatus(@PathVariable Long userId) {
        User userToCheck = userService.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        DeletionCheckDTO status = userService.checkAccountDeletionStatus(userToCheck);
        return ResponseEntity.ok(status);
    }
}