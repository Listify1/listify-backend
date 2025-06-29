package com.listify.backend.repository;

import com.listify.backend.model.Payment;
import com.listify.backend.model.PaymentSharedWith;
import com.listify.backend.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the {@link PaymentSharedWith} join entity.
 *
 * @author Listify Team
 * @version 1.0
 * @see PaymentSharedWith
 */
public interface PaymentSharedWithRepository extends JpaRepository<PaymentSharedWith, Long> {

    /**
     * Finds all share entries for a specific payment.
     *
     * @param payment The payment entity.
     * @return A list of {@link PaymentSharedWith} entries.
     */
    List<PaymentSharedWith> findAllByPayment(Payment payment);

    /**
     * Deletes all share entries associated with a specific payment ID.
     *
     * @param paymentId The ID of the payment.
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM PaymentSharedWith p WHERE p.payment.id = :paymentId")
    void deleteByPaymentId(@Param("paymentId") Long paymentId);

    /**
     * Deletes all share entries where the specified user is a participant.
     *
     * @param user The user whose share entries should be deleted.
     */
    @Modifying
    @Transactional
    void deleteAllBySharedWith(User user);
}

