package com.listify.backend.service;

import com.listify.backend.dto.DeletionCheckDTO;
import com.listify.backend.model.*;
import com.listify.backend.repository.*;
import com.listify.backend.model.enums.UserPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for managing {@link User} entities and related complex operations.
 * <p>
 * This service handles user data retrieval, saving, and the complex process of account deletion,
 * which involves checking for outstanding debts and cleaning up all related data across the application.
 *
 * @author Listify Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ShoppingListRepository shoppingListRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ItemRepository itemRepository;
    private final DebtRepository debtRepository;
    private final ProductSuggestionRepository productSuggestionRepository;
    private final GroupRepository groupRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentSharedWithRepository paymentSharedWithRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Checks if a user's account can be safely deleted.
     * It verifies if the user has outstanding debts OR if other members owe them money.
     * In both cases, the deletion is BLOCKED to ensure all financial matters are settled.
     *
     * @param currentUser The user whose account is to be checked.
     * @return A {@link DeletionCheckDTO} indicating the status (OK or BLOCKED) and a descriptive message.
     */
    public DeletionCheckDTO checkAccountDeletionStatus(User currentUser) {
        if (currentUser.getGroup() == null) {
            return new DeletionCheckDTO(DeletionCheckDTO.Status.OK, "Dein Account kann sicher gelöscht werden.");
        }

        // Nutzt die bereits vorhandene Methode, um alle relevanten Schulden zu holen
        List<com.listify.backend.model.Debt> debtsInvolvingUser = debtRepository.findAllDebtsInvolvingUser(currentUser);

        // 1. Prüfen, ob der User anderen Geld schuldet (höchste Priorität)
        double totalYouOwe = debtsInvolvingUser.stream()
                .filter(d -> d.getFrom() != null && d.getFrom().equals(currentUser))
                .mapToDouble(com.listify.backend.model.Debt::getAmount)
                .sum();

        if (totalYouOwe > 0.01) {
            return new DeletionCheckDTO(DeletionCheckDTO.Status.BLOCKED,
                    "Dein Account kann nicht gelöscht werden, da du anderen Mitgliedern noch Geld schuldest. Bitte begleiche zuerst deine Schulden.");
        }

        // 2. NEU: Prüfen, ob andere dem User Geld schulden und dies ebenfalls blockieren
        double totalOwedToYou = debtsInvolvingUser.stream()
                .filter(d -> d.getTo() != null && d.getTo().equals(currentUser))
                .mapToDouble(com.listify.backend.model.Debt::getAmount)
                .sum();

        if (totalOwedToYou > 0.01) {
            // Die Logik hier wurde von WARNING auf BLOCKED geändert, mit der neuen Nachricht.
            return new DeletionCheckDTO(DeletionCheckDTO.Status.BLOCKED,
                    String.format("Dein Account kann nicht gelöscht werden, da andere Mitglieder dir noch %.2f € schulden. Bitte klärt dies, bevor du den Account löschst.", totalOwedToYou));
        }

        // 3. Nur wenn beide Prüfungen negativ sind, ist das Löschen OK.
        return new DeletionCheckDTO(DeletionCheckDTO.Status.OK, "Dein Account kann sicher gelöscht werden.");
    }

    /**
     * Executes the complete, transactional deletion of a user account.
     * This includes reassigning admin roles, detaching the user from their group, anonymizing or
     * deleting all related data, and finally deleting the user record itself.
     *
     * @param userToDelete The user account to be deleted.
     * @throws ResponseStatusException if a final check shows that deletion is blocked.
     */
    @Transactional
    public void deleteUserAccount(User userToDelete) {
        System.out.println("Starte finalen Löschvorgang für Account: " + userToDelete.getEmail());

        DeletionCheckDTO finalCheck = checkAccountDeletionStatus(userToDelete);
        if (finalCheck.getStatus() == DeletionCheckDTO.Status.BLOCKED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Löschen nicht möglich, da noch Schulden bestehen.");
        }

        // Schritt 1: Admin-Rolle neu zuweisen, falls der User Admin in einer Gruppe ist.
        Group group = userToDelete.getGroup();
        if (group != null && userToDelete.getPermission() == UserPermission.ADMIN) {
            userRepository.findByGroupIdAndIdNot(group.getId(), userToDelete.getId())
                    .stream().findFirst().ifPresent(newAdmin -> {
                        newAdmin.setPermission(UserPermission.ADMIN);
                        userRepository.save(newAdmin);
                        System.out.println("Neuer Admin ernannt: " + newAdmin.getUsername());
                    });
        }

        // Schritt 2: Beziehung zur Gruppe sauber trennen
        if (group != null) {
            group.getUsers().remove(userToDelete);
            groupRepository.save(group);
            System.out.println("Beziehung zur Gruppe wurde sauber getrennt.");
        }

        // Schritt 3: Alle anderen Verknüpfungen kappen (Orphan Removal)
        itemRepository.findByAddedBy(userToDelete).forEach(item -> item.setAddedBy(null));
        itemRepository.findByBoughtBy(userToDelete).forEach(item -> item.setBoughtBy(null));
        chatMessageRepository.findBySender(userToDelete).forEach(msg -> msg.setSender(null));
        paymentRepository.findByPaidBy(userToDelete).forEach(payment -> payment.setPaidBy(null));
        System.out.println("Verknüpfungen von Zahlungen, Items, etc. wurden anonymisiert.");

        // Schritt 4: Alle direkt abhängigen Entitäten löschen
        // --- HIER IST DER NEUE, ENTSCHEIDENDE AUFRUF ---
        paymentSharedWithRepository.deleteAllBySharedWith(userToDelete);
        System.out.println("'Shared With'-Einträge für Zahlungen wurden entfernt.");

        debtRepository.deleteByFromOrTo(userToDelete, userToDelete);
        System.out.println("Schulden von/an " + userToDelete.getUsername() + " wurden entfernt.");

        productSuggestionRepository.deleteAllByCreatorEmail(userToDelete.getEmail());
        shoppingListRepository.deleteByOwnerEmailAndIsPrivateTrue(userToDelete.getEmail());

        // Schritt 5: Den User endgültig löschen
        userRepository.delete(userToDelete);
        System.out.println("Benutzer " + userToDelete.getEmail() + " vollständig gelöscht.");
    }

    /**
     * Deletes a user account identified by email.
     * @param email The email of the user to delete.
     */
    // Alte Methode bleibt, falls sie noch woanders gebraucht wird, löst aber nicht mehr den kompletten Flow aus
    @Transactional
    public void deleteAccountByEmail(String email) {
        User userToDelete = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Benutzer zum Löschen nicht gefunden: " + email));
        deleteUserAccount(userToDelete);
    }

    /**
     * Retrieves the permission level for a given user ID.
     * @param id The ID of the user.
     * @return The user's {@link UserPermission}.
     * @throws RuntimeException if the user is not found.
     */
    public UserPermission getPermissionByUserId(Long id) {
        return userRepository.findById(id)
                .map(User::getPermission)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

}
