package com.listify.backend.service;

import com.listify.backend.model.Group;
import com.listify.backend.model.User;
import com.listify.backend.repository.GroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for managing {@link Group} entities.
 *
 * @author Listify Team
 * @version 1.0
 */
@Service
public class GroupService {
    private final GroupRepository repo;
    public GroupService(GroupRepository repo) { this.repo = repo; }

    /**
     * Retrieves all groups.
     * @return A list of all groups.
     */
    public List<Group> findAll() { return repo.findAll(); }

    /**
     * Deletes a group by its ID.
     * @param id The ID of the group to delete.
     */
    public void delete(Long id) { repo.deleteById(id); }

    /**
     * Finds a group by its ID.
     * @param id The ID of the group to find.
     * @return An {@link Optional} containing the group if found.
     */
    public Optional<Group> findById(Long id) { return repo.findById(id); }

    /**
     * Generates a unique, short, uppercase join code.
     * @return A 6-character string.
     */
    private String generateJoinCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    /**
     * Saves or updates a group. If the group is new (i.e., has no join code),
     * a new code will be generated and assigned before saving.
     *
     * @param g The group entity to save.
     * @return The saved group entity with its join code.
     */
    public Group save(Group g) {
        if (g.getJoinCode() == null || g.getJoinCode().isEmpty()) {
            g.setJoinCode(generateJoinCode());
        }
        return repo.save(g);
    }

}
