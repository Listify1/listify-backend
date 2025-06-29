package com.listify.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Represents a single user activity within the application.
 * <p>
 * This entity is used to log significant actions performed by users,
 * such as creating a payment, joining a group, or adding an item.
 * It serves as an audit trail or a feed of recent events.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Activity {

    /**
     * The unique identifier for the activity record.
     * This is the primary key, generated automatically by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The category or type of the activity (e.g., "PAYMENT_CREATED", "USER_JOINED_GROUP").
     * This allows for easy filtering and rendering of different activity types.
     */
    private String type;

    /**
     * A human-readable description of the activity.
     * For example: "John Doe added Milk to the shopping list."
     */
    private String description;

    /**
     * The exact date and time when the activity occurred.
     */
    private LocalDateTime timestamp;

    /**
     * The user who performed the activity.
     * This establishes a many-to-one relationship, where many activities can be
     * associated with a single user.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}