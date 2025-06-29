package com.listify.backend.repository;

import com.listify.backend.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link Group} entity.
 *
 * @author Listify Team
 * @version 1.0
 * @see Group
 */
@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    /**
     * Finds a group by its unique join code.
     *
     * @param joinCode The unique code used to join the group.
     * @return An {@link Optional} containing the found {@link Group}, or {@link Optional#empty()} if not found.
     */
    Optional<Group> findByJoinCode(String joinCode);
}


