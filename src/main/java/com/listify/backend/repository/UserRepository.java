package com.listify.backend.repository;

import com.listify.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link User} entity.
 *
 * @author Listify Team
 * @version 1.0
 * @see User
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique email address.
     *
     * @param email The email address to search for.
     * @return An {@link Optional} containing the found {@link User}, or {@link Optional#empty()} if not found.
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds all users belonging to a specific group.
     *
     * @param groupId The ID of the group.
     * @return A list of {@link User}s who are members of the group.
     */
    List<User> findByGroupId(Long groupId);

    /**
     * Finds all users in a specific group, excluding one particular user by their ID.
     * This is useful for finding all "other" members of a group.
     *
     * @param groupId The ID of the group.
     * @param userId  The ID of the user to exclude from the result.
     * @return A list of {@link User}s in the group, excluding the specified user.
     */
    List<User> findByGroupIdAndIdNot(Long groupId, Long userId);
}