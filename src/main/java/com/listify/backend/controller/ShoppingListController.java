package com.listify.backend.controller;

import com.listify.backend.dto.AddItemsRequestDto;
import com.listify.backend.dto.ShoppingListDto;
import com.listify.backend.model.ShoppingList;
import com.listify.backend.model.User;
import com.listify.backend.service.ShoppingListService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing shopping lists and their items.
 * <p>
 * This class exposes REST endpoints under the {@code /api/shopping-lists} path
 * for creating, retrieving, deleting, and modifying shopping lists. It also provides
 * endpoints for fetching lists based on user context (e.g., own vs. shared lists).
 */
@RestController
@RequestMapping("/api/shopping-lists")
@CrossOrigin(origins = "*")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    /**
     * Constructs the controller and injects the required {@link ShoppingListService}.
     *
     * @param shoppingListService The service for handling shopping list business logic.
     */
    public ShoppingListController(ShoppingListService shoppingListService) {
        this.shoppingListService = shoppingListService;
    }

    /**
     * Retrieves all shopping lists.
     * Note: The scope of "all" is determined by the service implementation.
     *
     * @return A list of all {@link ShoppingList} entities.
     */
    @GetMapping
    public List<ShoppingList> getAllShoppingLists() {
        return shoppingListService.getAll();
    }

    /**
     * Retrieves a single shopping list by its unique ID.
     *
     * @param id The ID of the shopping list to retrieve.
     * @return The {@link ShoppingList} entity.
     */
    @GetMapping("/{id}")
    public ShoppingList getShoppingList(@PathVariable Long id) {
        return shoppingListService.getById(id);
    }

    /**
     * Deletes a shopping list by its unique ID.
     *
     * @param id The ID of the shopping list to delete.
     */
    @DeleteMapping("/{id}")
    public void deleteShoppingList(@PathVariable Long id) {
        shoppingListService.deleteById(id);
    }

    /**
     * Retrieves all lists visible to the current context.
     * (Functionally similar to getAllShoppingLists).
     *
     * @return A list of {@link ShoppingList} entities.
     */
    @GetMapping("/visible")
    public List<ShoppingList> getVisibleLists() {
        return shoppingListService.getAll();
    }

    /**
     * Retrieves all private shopping lists owned by the currently authenticated user.
     *
     * @return A list of the user's own {@link ShoppingList} entities.
     */
    @GetMapping("/own")
    public List<ShoppingList> getOwnLists() {
        return shoppingListService.getOwnLists();
    }

    /**
     * Retrieves all shared shopping lists associated with the group of the currently authenticated user.
     *
     * @return A list of shared {@link ShoppingList} entities.
     */
    @GetMapping("/shared")
    public List<ShoppingList> getSharedLists() {
        return shoppingListService.getSharedLists();
    }

    /**
     * Creates a new shopping list along with its initial items from a DTO.
     *
     * @param request The DTO containing the list's title and its initial items.
     * @return The newly created {@link ShoppingList} entity.
     */
    @PostMapping
    public ShoppingList createListWithItems(@RequestBody ShoppingListDto request) {
        return shoppingListService.createWithItems(request);
    }

    /**
     * Retrieves all shopping lists (both private and shared) for a specific user,
     * identified by email, including all associated items.
     *
     * @param email The email of the user whose lists are to be fetched.
     * @return A list of {@link ShoppingList} entities, populated with their items.
     */
    @GetMapping("/with-items")
    public List<ShoppingList> getListsWithItems(@RequestParam String email) {
        return shoppingListService.getListsWithItems(email);
    }

    /**
     * Retrieves a list of frequently purchased item names for the authenticated user.
     * <p>
     * This is used to provide suggestions to the user when creating new list items.
     *
     * @param user The currently authenticated user, injected by Spring Security.
     * @return A {@link ResponseEntity} containing a list of suggestion strings.
     */
    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getFrequentItemSuggestions(@AuthenticationPrincipal User user) {
        List<String> suggestions = shoppingListService.getFrequentItemSuggestions(user);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * Adds a batch of items to an existing shopping list.
     *
     * @param request     The DTO containing the target shopping list ID and the list of items to add.
     * @param currentUser The currently authenticated user, who will be set as the creator of the items.
     * @return A {@link ResponseEntity} with 200 OK on success.
     */
    @PostMapping("/add-items")
    public ResponseEntity<Void> addItemsToList(
            @RequestBody AddItemsRequestDto request,
            @AuthenticationPrincipal User currentUser) {
        shoppingListService.addItemsToList(request, currentUser);
        return ResponseEntity.ok().build();
    }
}