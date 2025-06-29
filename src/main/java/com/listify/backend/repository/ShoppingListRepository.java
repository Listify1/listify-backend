package com.listify.backend.repository;

import com.listify.backend.model.ShoppingList;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link ShoppingList} entity.
 *
 * @author Listify Team
 * @version 1.0
 * @see ShoppingList
 */
@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {

    /**
     * Finds all private shopping lists owned by a specific user.
     *
     * @param email The email of the owner.
     * @return A list of private {@link ShoppingList}s.
     */
    List<ShoppingList> findByOwnerEmailAndIsPrivateTrue(String email);

    /**
     * Fetches all shopping lists and eagerly loads their associated items
     * to prevent N+1 query problems.
     *
     * @return A list of all {@link ShoppingList}s with their items initialized.
     */
    @Query("SELECT DISTINCT sl FROM ShoppingList sl LEFT JOIN FETCH sl.items")
    List<ShoppingList> findAllWithItems();

    /**
     * Fetches a single shopping list by ID, eagerly loading its group and the group's users.
     *
     * @param id The ID of the shopping list.
     * @return An {@link Optional} containing the {@link ShoppingList} with its group and users, or empty if not found.
     */
    @Query("SELECT l FROM ShoppingList l LEFT JOIN FETCH l.group g LEFT JOIN FETCH g.users WHERE l.id = :id")
    Optional<ShoppingList> findWithGroupAndUsersById(@Param("id") Long id);

    /**
     * Deletes all items associated with a given shopping list ID.
     *
     * @param id The ID of the shopping list.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Item i WHERE i.shoppingList.id = :id")
    void deleteAllItemsByListId(@Param("id") Long id);

    /**
     * Deletes all private shopping lists owned by a specific user.
     *
     * @param email The email of the owner.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ShoppingList sl WHERE sl.ownerEmail = :email AND sl.isPrivate = true")
    void deleteByOwnerEmailAndIsPrivateTrue(@Param("email") String email);


}
