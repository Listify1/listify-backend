package com.listify.backend.repository;

import com.listify.backend.model.Payment;
import com.listify.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the {@link Payment} entity.
 *
 * @author Listify Team
 * @version 1.0
 * @see Payment
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Finds all payments, ordered by date in descending order.
     *
     * @return A list of all {@link Payment}s, with the most recent first.
     */
    List<Payment> findAllByOrderByDateDesc();

    /**
     * Finds all payments made by a specific user.
     *
     * @param user The user who made the payments.
     * @return A list of {@link Payment}s made by the user.
     */
    List<Payment> findByPaidBy(User user);

    /**
     * Finds all payments relevant to a given group. A payment is considered relevant
     * if any member of the group is part of the cost split.
     *
     * @param groupId The ID of the group.
     * @return A list of relevant {@link Payment}s, ordered by date descending.
     */
    @Query("SELECT DISTINCT p FROM Payment p " +
            "JOIN p.sharedWithEntries swe " +
            "WHERE swe.sharedWith.group.id = :groupId " +
            "ORDER BY p.date DESC, p.id DESC")
    List<Payment> findAllRelevantByGroupId(@Param("groupId") Long groupId);
}
