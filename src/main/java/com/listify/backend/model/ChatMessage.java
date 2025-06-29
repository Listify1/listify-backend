package com.listify.backend.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.listify.backend.model.enums.MessageType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a single message within a group chat.
 * <p>
 * This entity stores the content, sender, timestamp, and type of a message.
 * It also supports storing structured metadata in a JSONB column for special
 * message types like polls, using the hypersistence-utils library for mapping.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "messages")
@JsonInclude(JsonInclude.Include.NON_NULL) // Excludes null fields from JSON serialization.
public class ChatMessage {

    /**
     * The unique identifier for the chat message.
     * This is the primary key, generated automatically by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The textual content of the message. Mapped to a TEXT column
     * to allow for messages of arbitrary length.
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * The exact date and time when the message was created.
     */
    private LocalDateTime timestamp;

    /**
     * The type of the message (e.g., TEXT, POLL).
     * Stored as a string in the database for readability.
     */
    @Enumerated(EnumType.STRING)
    private MessageType type;

    /**
     * A map for storing arbitrary, structured metadata as a JSON object.
     * This is used for special message types, like storing poll options and vote counts.
     * The {@code @Type(JsonType.class)} annotation maps this field to a JSONB database column.
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /**
     * The user who sent the message.
     * Establishes a many-to-one relationship with the {@link User} entity.
     */
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    /**
     * The group chat to which this message belongs.
     * Establishes a many-to-one relationship with the {@link Group} entity.
     */
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;


    /**
     * Convenience method to safely get the ID of the associated group.
     *
     * @return The group ID, or {@code null} if the group is not set.
     */
    public Long getGroupId() {
        return group != null ? group.getId() : null;
    }

    /**
     * Convenience method to safely get the ID of the sender.
     *
     * @return The sender's user ID, or {@code null} if the sender is not set.
     */
    public Long getSenderId() {
        return sender != null ? sender.getId() : null;
    }

    /**
     * Convenience method to safely get the username of the sender.
     * Provides a fallback value if the sender has been deleted.
     *
     * @return The sender's username, or "Deleted User" if the sender is not set.
     */
    public String getSenderName() {
        return sender != null ? sender.getUsername() : "Deleted User";
    }
}