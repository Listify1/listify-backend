package com.listify.backend.service;

import com.listify.backend.dto.ItemDto;
import com.listify.backend.model.Item;
import com.listify.backend.model.ShoppingList;
import com.listify.backend.model.User;
import com.listify.backend.model.enums.ItemStatus;
import com.listify.backend.repository.ItemRepository;
import com.listify.backend.repository.ShoppingListRepository;
import com.listify.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for managing {@link Item} entities.
 * This service handles the business logic for creating, updating, deleting, and retrieving items,
 * ensuring they are correctly associated with shopping lists and users.
 *
 * @author Listify Team
 * @version 1.0
 */
@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final ShoppingListRepository shoppingListRepository;
    private final UserRepository userRepository;

    public ItemService(ItemRepository itemRepository, ShoppingListRepository shoppingListRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.shoppingListRepository = shoppingListRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates and saves a new item from a DTO.
     *
     * @param dto The DTO containing item data.
     * @param authUser The authenticated user adding the item.
     * @return The newly created {@link Item} entity.
     * @throws IllegalArgumentException if the shopping list ID is invalid.
     */
    public Item save(ItemDto dto, User authUser) {
        ShoppingList list = shoppingListRepository.findById(dto.getShoppingListId())
                .orElseThrow(() -> new IllegalArgumentException("ShoppingList ID ungültig"));

        Item item = new Item();
        item.setName(dto.getName());
        item.setQuantity(dto.getQuantity());
        item.setStatus(dto.getStatus() != null
                ? ItemStatus.valueOf(dto.getStatus().toUpperCase())
                : ItemStatus.OFFEN);
        item.setAddedBy(authUser);
        item.setShoppingList(list);

        if (dto.getBoughtById() != null) {
            userRepository.findById(dto.getBoughtById()).ifPresent(item::setBoughtBy);
        }

        return itemRepository.save(item);
    }

    /**
     * Updates an existing item.
     *
     * @param id The ID of the item to update.
     * @param updatedItem The item object with updated fields.
     * @param authUser The authenticated user performing the update, who will be set as the buyer.
     * @return The updated item entity.
     * @throws EntityNotFoundException if the item with the given ID is not found.
     */
    public Item update(Long id, Item updatedItem, User authUser) {
        return itemRepository.findById(id).map(item -> {
            item.setName(updatedItem.getName());
            item.setQuantity(updatedItem.getQuantity());
            item.setStatus(updatedItem.getStatus());

            item.setBoughtBy(authUser);

            return itemRepository.save(item);
        }).orElseThrow(() -> new EntityNotFoundException("Item nicht gefunden"));
    }

    /**
     * Deletes an item by its ID. This method also ensures the item is removed from its parent
     * shopping list's collection to correctly trigger orphan removal.
     *
     * @param id The ID of the item to delete.
     * @throws EntityNotFoundException if the item is not found.
     */
    @Transactional
    public void delete(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item nicht gefunden"));

        ShoppingList list = item.getShoppingList();
        list.getItems().removeIf(i -> i.getId().equals(id)); // wichtig bei EAGER/OrphanRemoval

        itemRepository.deleteById(id);
        itemRepository.flush(); // commit sofort in DB
    }

    /**
     * Retrieves all items.
     * @return A list of all items.
     */
    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    /**
     * Retrieves all items for a specific shopping list.
     * @param listId The ID of the shopping list.
     * @return A list of items.
     */
    public List<Item> findByShoppingListId(Long listId) {
        return itemRepository.findByShoppingListId(listId);
    }

    /**
     * Deletes all items associated with a shopping list.
     * @param listId The ID of the shopping list.
     */
    public void deleteAllItemsByListId(Long listId) {
        itemRepository.deleteByShoppingListId(listId);
        itemRepository.flush();
    }
}
