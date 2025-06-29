package com.listify.backend.dto;

import lombok.Data;

import java.util.List;

/**
 * Represents a request payload for creating a new financial payment.
 * <p>
 * This Data Transfer Object (DTO) is used to capture all the details of a transaction,
 * including who paid, how much was paid, for what, and how the cost is to be
 * distributed among other users.
 */
@Data
public class PaymentDTO {

    /**
     * A descriptive title for the payment (e.g., "Weekly Groceries", "Internet Bill").
     */
    public String title;

    /**
     * The total amount of the payment.
     */
    public double amount;

    /**
     * The date the payment was made, typically expected in "YYYY-MM-DD" format.
     */
    public String date;

    /**
     * The unique identifier of the user who made the payment.
     */
    public Long paidById;

    /**
     * An optional URL pointing to an image of a receipt or invoice for this payment.
     */
    public String imageUrl;

    /**
     * A list detailing how the payment is shared among users.
     * Each entry specifies a user and the portion of the amount they are responsible for.
     */
    public List<SharedUserDTO> sharedWith;

    /**
     * A nested static class representing a single user with whom a payment is shared.
     */
    public static class SharedUserDTO {

        /**
         * The unique identifier of the user who shares a portion of the cost.
         */
        public Long userId;

        /**
         * The specific amount of the total payment that this user is responsible for.
         */
        public double amount;

        /**
         * Default no-argument constructor.
         */
        public SharedUserDTO() {}
    }
}