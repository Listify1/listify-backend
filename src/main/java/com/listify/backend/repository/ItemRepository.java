package com.listify.backend.repository;

import com.listify.backend.model.Item;
import com.listify.backend.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the {@link Item} entity.
 *
 * @author Listify Team
 * @version 1.0
 * @see Item
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Finds all items belonging to a specific shopping list.
     *
     * @param shoppingListId The ID of the shopping list.
     * @return A list of {@link Item}s from the specified list.
     */
    List<Item> findByShoppingListId(Long shoppingListId);

    /**
     * Finds all items added by a specific user.
     *
     * @param user The user who added the items.
     * @return A list of {@link Item}s added by the user.
     */
    List<Item> findByAddedBy(User user);

    /**
     * Finds all items bought by a specific user.
     *
     * @param user The user who bought the items.
     * @return A list of {@link Item}s bought by the user.
     */
    List<Item> findByBoughtBy(User user);

    /**
     * Deletes all items belonging to a specific shopping list ID.
     *
     * @param listId The ID of the shopping list whose items are to be deleted.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Item i WHERE i.shoppingList.id = :listId")
    void deleteByShoppingListId(@Param("listId") Long listId);

    /**
     * Retrieves a list of the most frequently bought item names from a user's private lists.
     *
     * @param email The email of the list owner.
     * @return A list of item names, ordered by purchase frequency.
     */
    @Query("SELECT i.name FROM Item i WHERE i.shoppingList.ownerEmail = :email AND i.shoppingList.isPrivate = true AND i.status = 'GEKAUFT' GROUP BY i.name ORDER BY COUNT(i.name) DESC")
    List<String> findFrequentlyBoughtItemNamesByOwnerEmail(@Param("email") String email);
}

