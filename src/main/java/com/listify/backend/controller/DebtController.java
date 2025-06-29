package com.listify.backend.controller;

import com.listify.backend.service.DebtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling debt-related operations.
 * <p>
 * This class exposes REST endpoints under the {@code /api/debts} path
 * to manage financial debts between users, such as settling outstanding balances.
 */
@RestController
@RequestMapping("/api/debts")
@RequiredArgsConstructor
public class DebtController {

    /**
     * The service responsible for handling the business logic of debt management.
     * Injected via constructor by Lombok's {@code @RequiredArgsConstructor}.
     */
    private final DebtService debtService;

    /**
     * Settles all outstanding debts between two specified users.
     * <p>
     * This endpoint marks all debts from a 'fromUser' to a 'toUser' as settled.
     * It's typically used when one user confirms they have paid another user back.
     * The operation is idempotent.
     *
     * @param fromUserId the ID of the user who owed the money (the debtor).
     * @param toUserId   the ID of the user who was owed the money (the creditor).
     * @return a {@link ResponseEntity} with a 204 No Content status upon successful settlement.
     */
    @DeleteMapping("/settle")
    public ResponseEntity<Void> settleDebts(@RequestParam Long fromUserId, @RequestParam Long toUserId) {
        debtService.settleDebtsBetweenUsers(fromUserId, toUserId);
        // A 204 No Content response is appropriate for a successful settlement/deletion action.
        return ResponseEntity.noContent().build();
    }
}