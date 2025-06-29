package com.listify.backend.service;

import com.listify.backend.dto.DebtSummaryDTO;
import com.listify.backend.dto.PaymentDTO;
import com.listify.backend.dto.PaymentResponseDTO;
import com.listify.backend.dto.PaymentResponseDTO.UserDTO;
import com.listify.backend.model.Debt;
import com.listify.backend.model.Payment;
import com.listify.backend.model.PaymentSharedWith;
import com.listify.backend.model.User;
import com.listify.backend.repository.DebtRepository;
import com.listify.backend.repository.PaymentRepository;
import com.listify.backend.repository.PaymentSharedWithRepository;
import com.listify.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service layer for handling complex payment and debt logic.
 * This service orchestrates the creation of payments, the automatic generation of corresponding
 * debts, and the calculation of financial summaries for users within a group.
 *
 * @author Listify Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepo;
    private final DebtRepository debtRepository;
    private final UserRepository userRepository;
    private final PaymentSharedWithRepository paymentSharedWithRepo;

    /**
     * Creates a new payment and automatically generates the associated debt records.
     * The process is transactional, ensuring that either the payment and all debts are created, or nothing is.
     *
     * @param dto The DTO containing all necessary information for the payment.
     * @return The newly created {@link Payment} entity.
     * @throws RuntimeException if a user specified in the DTO is not found.
     * @throws IllegalArgumentException if the payment is not shared with anyone.
     */
    @Transactional
    public Payment createPaymentFromDTO(PaymentDTO dto) {
        logger.info("Erstelle Zahlung aus DTO: {}", dto);
        User paidBy = userRepository.findById(dto.paidById)
                .orElseThrow(() -> new RuntimeException("Zahler mit ID " + dto.paidById + " nicht gefunden"));

        int numberOfParticipants = dto.sharedWith.size();
        if (numberOfParticipants == 0) {
            throw new IllegalArgumentException("Die Zahlung muss mit mindestens einer Person geteilt werden.");
        }
        double amountPerUser = dto.amount / numberOfParticipants;

        Payment payment = new Payment();
        payment.setTitle(dto.title);
        payment.setAmount(dto.amount);
        payment.setDate(LocalDate.parse(dto.date));
        payment.setPaidBy(paidBy);
        payment.setImageUrl(dto.getImageUrl());

        Payment savedPayment = paymentRepo.save(payment);

        List<PaymentSharedWith> sharedWithEntries = new ArrayList<>();
        for (PaymentDTO.SharedUserDTO sharedUserDto : dto.sharedWith) {
            User sharedUser = userRepository.findById(sharedUserDto.userId)
                    .orElseThrow(() -> new RuntimeException("Mitnutzer mit ID " + sharedUserDto.userId + " nicht gefunden"));
            PaymentSharedWith psw = new PaymentSharedWith();
            psw.setPayment(savedPayment);
            psw.setSharedWith(sharedUser);
            sharedWithEntries.add(psw);

            if (!sharedUser.getId().equals(paidBy.getId())) {
                Debt newDebt = new Debt();
                newDebt.setFrom(sharedUser);
                newDebt.setTo(paidBy);
                newDebt.setAmount(amountPerUser);
                newDebt.setPayment(savedPayment);
                newDebt.setReason(payment.getTitle());
                newDebt.setGroup(paidBy.getGroup());
                debtRepository.save(newDebt);
            }
        }
        paymentSharedWithRepo.saveAll(sharedWithEntries);
        savedPayment.setSharedWithEntries(sharedWithEntries);
        return savedPayment;
    }

    /**
     * Converts a {@link Payment} entity to a {@link PaymentResponseDTO}.
     * This method handles cases where associated users might have been deleted and normalizes avatar URLs.
     *
     * @param payment The payment entity to convert.
     * @return A DTO suitable for sending to the client.
     */
    public PaymentResponseDTO toResponseDTO(Payment payment) {
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.id = payment.getId();
        dto.title = payment.getTitle();
        dto.amount = payment.getAmount();
        dto.date = payment.getDate().toString();
        dto.imageUrl = payment.getImageUrl();

        User paidBy = payment.getPaidBy();
        if (paidBy != null) {
            UserDTO paidByDto = new UserDTO();
            paidByDto.id = paidBy.getId();
            paidByDto.username = paidBy.getUsername();

            // === KORREKTUR FÜR 'paidBy' START ===
            String avatarUrl = paidBy.getAvatarUrl();
            if (avatarUrl == null || avatarUrl.isBlank()) {
                avatarUrl = "https://api.dicebear.com/7.x/personas/png?seed=" + paidBy.getUsername();
            } else if (avatarUrl.contains("/svg")) {
                avatarUrl = avatarUrl.replace("/svg", "/png");
            }
            paidByDto.avatarUrl = avatarUrl;
            // === KORREKTUR FÜR 'paidBy' ENDE ===

            dto.paidBy = paidByDto;
        } else {
            UserDTO deletedUserDto = new UserDTO();
            deletedUserDto.id = -1L;
            deletedUserDto.username = "Gelöschter Nutzer";
            deletedUserDto.avatarUrl = "https://api.dicebear.com/7.x/bottts-neutral/png?seed=deletedUser";
            dto.paidBy = deletedUserDto;
        }

        List<PaymentSharedWith> shared = paymentSharedWithRepo.findAllByPayment(payment);
        dto.sharedWith = shared.stream()
                .map(psw -> {
                    User user = psw.getSharedWith();
                    if (user != null) {
                        UserDTO uDto = new UserDTO();
                        uDto.id = user.getId();
                        uDto.username = user.getUsername();

                        // === KORREKTUR FÜR 'sharedWith' START ===
                        String sharedAvatarUrl = user.getAvatarUrl();
                        if (sharedAvatarUrl == null || sharedAvatarUrl.isBlank()) {
                            sharedAvatarUrl = "https://api.dicebear.com/7.x/personas/png?seed=" + user.getUsername();
                        } else if (sharedAvatarUrl.contains("/svg")) {
                            sharedAvatarUrl = sharedAvatarUrl.replace("/svg", "/png");
                        }
                        uDto.avatarUrl = sharedAvatarUrl;
                        // === KORREKTUR FÜR 'sharedWith' ENDE ===

                        return uDto;
                    }
                    return null;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        return dto;
    }

    /**
     * Retrieves all payments and converts them to response DTOs, ordered by date.
     * @return A list of {@link PaymentResponseDTO}s.
     */
    public List<PaymentResponseDTO> getAllPaymentResponses() {
        return paymentRepo.findAllByOrderByDateDesc()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Generates a comprehensive financial summary for a given user within their group.
     * This includes total amounts owed, detailed balances with each member, and recent payments.
     *
     * @param currentUser The user for whom to generate the summary.
     * @return A map containing the summary data, with keys "totalOwedToYou", "totalYouOwe", "debtSummaries", and "recentPayments".
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getPaymentSummary(User currentUser) {
        if (currentUser.getGroup() == null) {
            Map<String, Object> emptySummary = new HashMap<>();
            emptySummary.put("totalOwedToYou", 0.0);
            emptySummary.put("totalYouOwe", 0.0);
            emptySummary.put("debtSummaries", new ArrayList<>());
            emptySummary.put("recentPayments", new ArrayList<>());
            return emptySummary;
        }
        Long groupId = currentUser.getGroup().getId();
        Long currentUserId = currentUser.getId();

        List<Debt> allDebtsInGroup = debtRepository.findByGroupId(groupId);
        List<User> groupMembers = userRepository.findByGroupId(groupId);

        List<DebtSummaryDTO> debtSummaries = groupMembers.stream()
                .filter(member -> !member.getId().equals(currentUserId))
                .map(member -> {
                    Long memberId = member.getId();

                    double totalMemberOwesYou = allDebtsInGroup.stream()
                            .filter(d -> d.getFrom() != null && d.getFrom().getId().equals(memberId) && d.getTo() != null && d.getTo().getId().equals(currentUserId))
                            .mapToDouble(Debt::getAmount).sum();
                    double totalYouOweMember = allDebtsInGroup.stream()
                            .filter(d -> d.getFrom() != null && d.getFrom().getId().equals(currentUserId) && d.getTo() != null && d.getTo().getId().equals(memberId))
                            .mapToDouble(Debt::getAmount).sum();
                    double netDifference = totalMemberOwesYou - totalYouOweMember;
                    double finalOwesYou = Math.max(0, netDifference);
                    double finalYouOwe = Math.max(0, -netDifference);

                    String avatarUrl = member.getAvatarUrl();

                    if (avatarUrl == null || avatarUrl.isBlank()) {
                        avatarUrl = "https://api.dicebear.com/7.x/personas/png?seed=" + member.getUsername();
                    } else if (avatarUrl.contains("/svg")) {
                        avatarUrl = avatarUrl.replace("/svg", "/png");
                    }
                    return new DebtSummaryDTO(memberId, member.getUsername(), avatarUrl, finalOwesYou, finalYouOwe);
                })
                .collect(Collectors.toList());

        double totalOwedToYou = debtSummaries.stream().mapToDouble(DebtSummaryDTO::getOwesYou).sum();
        double totalYouOwe = debtSummaries.stream().mapToDouble(DebtSummaryDTO::getYouOwe).sum();

        List<Payment> relevantPaymentsInDb = paymentRepo.findAllRelevantByGroupId(groupId);
        List<PaymentResponseDTO> recentPayments = relevantPaymentsInDb.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalOwedToYou", totalOwedToYou);
        summary.put("totalYouOwe", totalYouOwe);
        summary.put("debtSummaries", debtSummaries);
        summary.put("recentPayments", recentPayments);

        return summary;
    }
}