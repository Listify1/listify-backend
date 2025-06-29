package com.listify.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a summarized view of debts between the current user and another specific user.
 * <p>
 * This Data Transfer Object (DTO) aggregates all financial transactions into two main figures:
 * the total amount the other user owes the current user, and the total amount the current
 * user owes the other user. It also includes identifying details for the other user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DebtSummaryDTO {

    /**
     * The unique identifier of the other user involved in the debt relationship.
     */
    private Long userId;

    /**
     * The username of the other user.
     */
    private String username;

    /**
     * The URL for the avatar image of the other user.
     */
    private String avatarUrl;

    /**
     * The total amount of money that this user owes to the currently authenticated user.
     */
    private double owesYou;

    /**
     * The total amount of money that the currently authenticated user owes to this user.
     */
    private double youOwe;
}