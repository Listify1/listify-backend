package com.listify.backend.service;

import com.listify.backend.model.Debt;
import com.listify.backend.model.User;
import com.listify.backend.repository.DebtRepository;
import com.listify.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for managing {@link Debt} entities.
 * Provides business logic for creating, retrieving, and deleting debt records.
 *
 * @author Listify Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class DebtService {
    private final DebtRepository debtRepository;
    private final UserRepository userRepository;

    /**
     * Saves or updates a debt record.
     * @param debt The debt entity to save.
     * @return The saved debt entity.
     */
    public Debt save(Debt debt) {
        return debtRepository.save(debt);
    }

    /**
     * Retrieves all debt records.
     * @return A list of all debts.
     */
    public List<Debt> getAll() {
        return debtRepository.findAll();
    }

    /**
     * Retrieves a single debt record by its ID.
     * @param id The ID of the debt to retrieve.
     * @return The found debt, or {@code null} if not found.
     */
    public Debt getById(Long id) {
        return debtRepository.findById(id).orElse(null);
    }

    /**
     * Deletes a debt record by its ID.
     * @param id The ID of the debt to delete.
     */
    public void deleteById(Long id) {
        debtRepository.deleteById(id);
    }

    /**
     * Settles all outstanding debts between two users by deleting the corresponding records.
     * This operation is transactional.
     *
     * @param fromUserId The ID of one user.
     * @param toUserId The ID of the other user.
     */
    @Transactional
    public void settleDebtsBetweenUsers(Long fromUserId, Long toUserId) {
        debtRepository.deleteDebtsFromUserToUser(fromUserId, toUserId);
    }
}
