package com.listify.backend.controller;

import com.listify.backend.dto.ItemDto;
import com.listify.backend.model.Item;
import com.listify.backend.model.User;
import com.listify.backend.model.enums.ItemStatus;
import com.listify.backend.service.ItemService;
import com.listify.backend.service.ProductSuggestionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing shopping list items.
 * <p>
 * This class exposes REST endpoints under the {@code /api/items} path for creating,
 * updating, deleting, and retrieving items associated with shopping lists.
 */
@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService service;
    private final ProductSuggestionService suggestionService;

    /**
     * Constructs the controller and injects required dependencies.
     *
     * @param service           The service for handling item business logic.
     * @param suggestionService The service for product suggestions.
     */
    public ItemController(ItemService service, ProductSuggestionService suggestionService) {
        this.service = service;
        this.suggestionService = suggestionService;
    }

    /**
     * Creates a new item and adds it to a shopping list.
     * <p>
     * The currently authenticated user is automatically set as the creator of the item.
     *
     * @param dto      The data transfer object containing the item's details, including the shoppingListId.
     * @param authUser The currently authenticated user, injected by Spring Security.
     * @return The newly created {@link Item} entity.
     * @throws IllegalArgumentException if the shoppingListId is not provided in the request body.
     */
    @PostMapping
    public Item create(@RequestBody ItemDto dto, @AuthenticationPrincipal User authUser) {
        if (dto.getShoppingListId() == null) {
            throw new IllegalArgumentException("Item must include shoppingListId");
        }
        return service.save(dto, authUser);
    }

    /**
     * Updates an existing item.
     * <p>
     * This can be used to change an item's name, quantity, or status (e.g., from 'OFFEN' to 'GEKAUFT').
     *
     * @param id          The ID of the item to update.
     * @param updatedItem The item object with the updated fields.
     * @param authUser    The currently authenticated user, for potential authorization checks in the service layer.
     * @return The updated {@link Item} entity.
     */
    @PutMapping("/{id}")
    public Item update(@PathVariable Long id, @RequestBody Item updatedItem, @AuthenticationPrincipal User authUser) {
        return service.update(id, updatedItem, authUser);
    }

    /**
     * Deletes an item by its unique ID.
     *
     * @param id The ID of the item to delete.
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    /**
     * Retrieves all items associated with a specific shopping list.
     *
     * @param listId The ID of the shopping list.
     * @return A list of all {@link Item} entities for the given list.
     */
    @GetMapping("/by-list/{listId}")
    public List<Item> getItemsByListId(@PathVariable Long listId) {
        return service.findByShoppingListId(listId);
    }

    /**
     * Retrieves only the 'open' (not yet purchased) items for a specific shopping list.
     *
     * @param listId The ID of the shopping list.
     * @return A filtered list of {@link Item} entities with the status {@code ItemStatus.OFFEN}.
     */
    @GetMapping("/by-list/{listId}/offen")
    public List<Item> getOffeneItemsByListId(@PathVariable Long listId) {
        return service.findByShoppingListId(listId)
                .stream()
                .filter(item -> item.getStatus() == ItemStatus.OFFEN)
                .toList();
    }

}