package com.listify.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the result of a pre-deletion or pre-removal check for a user account.
 * <p>
 * This Data Transfer Object (DTO) is used to inform the client whether a sensitive
 * operation (like deleting an account or removing a user from a group) can proceed.
 * It provides a clear status and a human-readable message explaining any potential issues.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeletionCheckDTO {

    /**
     * The machine-readable status of the deletion check, indicating the severity level.
     */
    private Status status;

    /**
     * A human-readable message describing the status. This message is intended to be
     * displayed directly to the user.
     */
    private String message;

    /**
     * An enumeration representing the possible outcomes of a deletion check.
     */
    public enum Status {
        /**
         * The operation can be performed without any warnings or blocking issues.
         */
        OK,

        /**
         * The operation can be performed, but there is a non-critical issue that the
         * user should be warned about (e.g., other users still owe this user money,
         * which will be forfeited).
         */
        WARNING,

        /**
         * The operation cannot be performed because of a critical, blocking issue
         * (e.g., the user still owes money to other members of the group).
         */
        BLOCKED
    }
}