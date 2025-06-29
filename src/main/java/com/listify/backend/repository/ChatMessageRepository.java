package com.listify.backend.repository;

import com.listify.backend.model.ChatMessage;
import com.listify.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for the {@link ChatMessage} entity.
 * This repository provides standard CRUD operations for chat messages and
 * includes custom query methods to retrieve messages based on specific criteria.
 *
 * @author Listify Team
 * @version 1.0
 * @see ChatMessage
 */
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Finds all chat messages sent by a specific user.
     *
     * @param user The user who sent the messages.
     * @return A list of {@link ChatMessage} entities sent by the given user.
     */
    List<ChatMessage> findBySender(User user);

    /**
     * Finds all chat messages belonging to a specific group, identified by its ID.
     *
     * @param groupId The ID of the group.
     * @return A list of {@link ChatMessage} entities associated with the given group ID.
     */
    List<ChatMessage> findByGroup_Id(Long groupId);
}