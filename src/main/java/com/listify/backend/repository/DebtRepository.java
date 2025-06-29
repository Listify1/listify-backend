package com.listify.backend.repository;

import com.listify.backend.model.Debt;
import com.listify.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Spring Data JPA repository for the {@link Debt} entity.
 * Manages CRUD operations for debts and provides custom queries for complex
 * debt retrieval and bulk deletion operations.
 *
 * @author Listify Team
 * @version 1.0
 * @see Debt
 */
public interface DebtRepository extends JpaRepository<Debt, Long> {

    /**
     * Retrieves all debts associated with a specific group ID.
     *
     * @param groupId The ID of the group to find debts for.
     * @return A list of {@link Debt} entities for the specified group.
     */
    List<Debt> findByGroupId(Long groupId);

    /**
     * Deletes all debt records associated with a specific payment ID in a single operation.
     * This is a modifying query and must be executed within a transaction.
     *
     * @param paymentId The ID of the payment whose related debts should be deleted.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Debt d WHERE d.payment.id = :paymentId")
    void deleteByPaymentId(@Param("paymentId") Long paymentId);

    /**
     * Deletes all debts between two specific users.
     * This is a modifying query and must be executed within a transaction.
     *
     * @param fromUserId The ID of one user.
     * @param toUserId   The ID of the other user.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Debt d WHERE (d.from.id = :fromUserId AND d.to.id = :toUserId) OR (d.from.id = :toUserId AND d.to.id = :fromUserId)")
    void deleteDebtsFromUserToUser(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);

    /**
     * Deletes all debt records where the given user is either the debtor or the creditor.
     * This is useful for cleaning up user data upon account deletion.
     *
     * @param fromUser The user as a debtor.
     * @param toUser   The user as a creditor.
     */
    @Modifying
    @Transactional
    void deleteByFromOrTo(User fromUser, User toUser);

    /**
     * Finds all debts where the specified user is either the debtor ('from') or the creditor ('to').
     * This is used for checking if an account can be deleted.
     *
     * @param user The user to check for.
     * @return A list of all debts involving the user.
     */
    @Query("SELECT d FROM Debt d WHERE d.from = :user OR d.to = :user")
    List<Debt> findAllDebtsInvolvingUser(@Param("user") User user);
}