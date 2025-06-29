package com.listify.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Represents a single, one-way financial obligation from one user to another.
 * <p>
 * A {@code Debt} record is typically created automatically when a {@link Payment} is shared.
 * For example, if User A pays $10 and shares it equally with User B, a debt record
 * is created indicating that User B owes User A $5.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Debt {

    /**
     * The unique identifier for the debt record.
     * This is the primary key, generated automatically by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who owes the money (the debtor).
     * Establishes a many-to-one relationship with the {@link User} entity.
     */
    @ManyToOne
    @JoinColumn(name = "from_user_id")
    private User from;

    /**
     * The user who is owed the money (the creditor).
     * Establishes a many-to-one relationship with the {@link User} entity.
     */
    @ManyToOne
    @JoinColumn(name = "to_user_id")
    private User to;

    /**
     * The monetary value of this specific debt obligation.
     */
    private double amount;

    /**
     * A description of the reason for the debt, usually derived from the
     * title of the associated payment.
     */
    private String reason;

    /**
     * The exact date and time when the debt was created.
     */
    private LocalDateTime timestamp;

    /**
     * The group within which this debt was incurred.
     * This provides context and helps scope financial summaries.
     */
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    /**
     * The original payment that generated this debt.
     * This creates a link back to the source transaction for traceability.
     */
    @ManyToOne
    private Payment payment;

}