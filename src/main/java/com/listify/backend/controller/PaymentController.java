package com.listify.backend.controller;

import com.listify.backend.dto.PaymentDTO;
import com.listify.backend.dto.PaymentResponseDTO;
import com.listify.backend.model.Payment;
import com.listify.backend.model.User;
import com.listify.backend.repository.DebtRepository;
import com.listify.backend.repository.PaymentRepository;
import com.listify.backend.repository.PaymentSharedWithRepository;
import com.listify.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for handling financial payments and transactions.
 * <p>
 * This class exposes REST endpoints under the {@code /api/payments} path for creating,
 * retrieving, and deleting payments, as well as providing a summary of user-related debts and credits.
 */
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = {"http://10.0.2.2:8081", "http://localhost:8081"})
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final PaymentSharedWithRepository paymentSharedWithRepository;
    private final DebtRepository debtRepository;

    /**
     * Creates a new payment and calculates the resulting debts among group members.
     *
     * @param dto The data transfer object containing the details of the payment.
     * @return a {@link ResponseEntity} containing the created {@link PaymentResponseDTO} on success,
     * or an internal server error on failure.
     */
    @PostMapping
    public ResponseEntity<?> createPayment(@RequestBody PaymentDTO dto) {
        System.out.println("📡 POST /api/payments");
        try {
            Payment saved = paymentService.createPaymentFromDTO(dto);
            PaymentResponseDTO response = paymentService.toResponseDTO(saved);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error while saving payment: " + e.getMessage());
        }
    }

    /**
     * Retrieves a list of all payments.
     *
     * @return A list of {@link PaymentResponseDTO} objects for all recorded payments.
     */
    @GetMapping
    public List<PaymentResponseDTO> getAllPayments() {
        return paymentService.getAllPaymentResponses();
    }

    /**
     * Deletes a payment and its associated data, including sharing information and resulting debts.
     *
     * @param id The unique ID of the payment to be deleted.
     * @return a {@link ResponseEntity} with 204 No Content on successful deletion,
     * or 404 Not Found if the payment does not exist.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        if (!paymentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        // Manually cascade delete to related entities.
        debtRepository.deleteByPaymentId(id);
        paymentSharedWithRepository.deleteByPaymentId(id);
        paymentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Calculates and returns a financial summary for the currently authenticated user.
     * <p>
     * The summary includes who the user owes money to and who owes money to the user,
     * aggregating all debts and credits within their group.
     *
     * @param currentUser The currently authenticated user, injected by Spring Security.
     * @return a {@link ResponseEntity} containing a map with the payment summary,
     * or a 400 Bad Request if the user is not part of a group.
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getPaymentSummary(@AuthenticationPrincipal User currentUser) {
        if (currentUser.getGroup() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "User is not in a group"));
        }
        Map<String, Object> summary = paymentService.getPaymentSummary(currentUser);
        return ResponseEntity.ok(summary);
    }
}