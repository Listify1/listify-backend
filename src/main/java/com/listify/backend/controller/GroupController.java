package com.listify.backend.controller;

import com.listify.backend.model.Group;
import com.listify.backend.model.User;
import com.listify.backend.model.enums.UserPermission;
import com.listify.backend.repository.GroupRepository;
import com.listify.backend.repository.UserRepository;
import com.listify.backend.service.GroupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;

/**
 * Controller for managing groups and group memberships.
 * <p>
 * This class exposes REST endpoints for creating, retrieving, and deleting groups,
 * as well as handling user associations like joining, leaving, and being removed from a group.
 */
@RestController
@RequestMapping("/api/groups")
public class GroupController {
    private final GroupService service;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    /**
     * Constructs the controller and injects required dependencies.
     *
     * @param service         The service for basic group operations.
     * @param groupRepository The repository for direct group data access.
     * @param userRepository  The repository for direct user data access.
     */
    public GroupController(GroupService service, GroupRepository groupRepository, UserRepository userRepository) {
        this.service = service;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new group. (Note: Deprecated in practice by createWithUser).
     *
     * @param g The group object to create.
     * @return The saved group entity.
     */
    @PostMapping
    public Group create(@RequestBody Group g) {
        return service.save(g);
    }

    /**
     * Retrieves a list of all existing groups.
     *
     * @return A list of all groups.
     */
    @GetMapping
    public List<Group> all() {
        return service.findAll();
    }

    /**
     * Deletes a group by its unique ID.
     *
     * @param id The ID of the group to delete.
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    /**
     * Adds a specific user to a specific group.
     *
     * @param groupId The ID of the group to join.
     * @param userId  The ID of the user to be added.
     * @return A {@link ResponseEntity} with a success message or a 404 error if not found.
     */
    @PatchMapping("/{groupId}/add-user/{userId}")
    public ResponseEntity<String> addUserToGroup(@PathVariable Long groupId, @PathVariable Long userId) {

        Optional<Group> groupOpt = groupRepository.findById(groupId);

        Optional<User> userOpt = userRepository.findById(userId);

        if (groupOpt.isEmpty() || userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group or User not found");
        }

        Group group = groupOpt.get();
        User user = userOpt.get();

        if (!group.getUsers().contains(user)) {
            group.getUsers().add(user);
        }

        user.setGroup(group);
        user.setPermission(UserPermission.ROOMMATE);
        userRepository.save(user);
        groupRepository.save(group);

        return ResponseEntity.ok("User added to group");
    }

    /**
     * Creates a new group and assigns the specified user as its initial administrator.
     * <p>
     * This method generates a unique join code for the group and sets the creating user's
     * permission level to ADMIN.
     *
     * @param group  The group object containing initial details like the name.
     * @param userId The ID of the user creating the group.
     * @return A {@link ResponseEntity} containing the newly created group.
     */
    @PostMapping("/create-with-user")
    public ResponseEntity<Group> createWithUser(@RequestBody Group group, @RequestParam Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User user = userOpt.get();
        if (group.getJoinCode() == null || group.getJoinCode().isEmpty()) {
            group.setJoinCode(UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        }

        // Add user to the group's member list.
        group.getUsers().add(user);
        Group savedGroup = groupRepository.save(group);

        // Associate the group with the user and set them as ADMIN.
        user.setGroup(savedGroup);
        user.setPermission(UserPermission.ADMIN);
        userRepository.save(user);

        return ResponseEntity.ok(savedGroup);
    }


    /**
     * Finds a group by its unique, case-insensitive join code.
     *
     * @param joinCode The 6-character code used to join the group.
     * @return The group entity if found (200 OK), or a 404 Not Found response.
     */
    @GetMapping("/code/{joinCode}")
    public ResponseEntity<Group> findByJoinCode(@PathVariable String joinCode) {
        return groupRepository.findByJoinCode(joinCode.trim().toUpperCase())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Retrieves the group associated with a specific user.
     *
     * @param id The ID of the user.
     * @return The user's group (200 OK), or 204 No Content if the user is not in a group.
     */
    @GetMapping("/users/{id}/group")
    public ResponseEntity<Group> getGroupByUserId(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User user = userOpt.get();
        Group group = user.getGroup();

        if (group == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        return ResponseEntity.ok(group);
    }

    /**
     * Removes a user from a group, for instance, when a user chooses to leave.
     * <p>
     * If the user leaving was an admin, this method automatically promotes the
     * first remaining member to be the new admin to ensure the group remains manageable.
     *
     * @param groupId The ID of the group to leave.
     * @param userId  The ID of the user leaving.
     * @return A {@link ResponseEntity} with a success or error message.
     */
    @PatchMapping("/{groupId}/remove-user/{userId}")
    public ResponseEntity<String> removeUserFromGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        Optional<Group> groupOpt = groupRepository.findById(groupId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (groupOpt.isEmpty() || userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group or User not found");
        }

        Group group = groupOpt.get();
        User user = userOpt.get();

        if (!group.getUsers().contains(user)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not a member of the group");
        }

        boolean wasAdmin = user.getPermission() == UserPermission.ADMIN;

        // Dissociate user from group.
        group.getUsers().remove(user);
        user.setGroup(null);
        user.setPermission(null);
        userRepository.save(user);

        // If the admin left, assign a new admin.
        if (wasAdmin && !group.getUsers().isEmpty()) {
            User newAdmin = group.getUsers().get(0);
            newAdmin.setPermission(UserPermission.ADMIN);
            userRepository.save(newAdmin);
        }

        groupRepository.save(group);

        return ResponseEntity.ok("User removed from group");
    }

    /**
     * Allows an admin to remove (kick) another user from a group.
     *
     * @param groupId      The ID of the group.
     * @param targetUserId The ID of the user to be removed.
     * @param adminUserId  The ID of the admin performing the action (for validation).
     * @return A {@link ResponseEntity} containing a message and the updated group, or an error.
     */
    @PatchMapping("/{groupId}/kick-user/{targetUserId}")
    public ResponseEntity<?> kickUserFromGroup(
            @PathVariable Long groupId,
            @PathVariable Long targetUserId,
            @RequestParam Long adminUserId) {

        Optional<Group> groupOpt = groupRepository.findById(groupId);
        Optional<User> adminOpt = userRepository.findById(adminUserId);
        Optional<User> targetOpt = userRepository.findById(targetUserId);

        if (groupOpt.isEmpty() || adminOpt.isEmpty() || targetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Group or User not found"));
        }

        Group group = groupOpt.get();
        User admin = adminOpt.get();
        User target = targetOpt.get();

        // Validate that both users belong to the specified group.
        if (!group.getUsers().contains(admin) || !group.getUsers().contains(target)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Users not in the same group"));
        }

        // Validate that the requester is an admin.
        if (admin.getPermission() != UserPermission.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Only Admins can remove users"));
        }

        // Admins cannot kick themselves.
        if (admin.getId().equals(target.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Admins cannot remove themselves"));
        }

        // Perform the removal.
        group.getUsers().remove(target);
        target.setGroup(null);
        target.setPermission(null);

        userRepository.save(target);
        groupRepository.save(group);

        Group updatedGroup = groupRepository.findById(groupId).orElse(null);
        return ResponseEntity.ok(Map.of(
                "message", "User removed from group",
                "group", updatedGroup
        ));
    }
}