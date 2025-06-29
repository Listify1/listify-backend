package com.listify.backend.service;

import com.listify.backend.dto.AddItemsRequestDto;
import com.listify.backend.dto.ShoppingListDto;
import com.listify.backend.model.Item;
import com.listify.backend.model.ShoppingList;
import com.listify.backend.model.User;
import com.listify.backend.model.enums.ItemStatus;
import com.listify.backend.repository.GroupRepository;
import com.listify.backend.repository.ItemRepository;
import com.listify.backend.repository.ShoppingListRepository;
import com.listify.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service layer for managing {@link ShoppingList} entities and their associated items.
 *
 * @author Listify Team
 * @version 1.0
 */
@Service
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;
    private final ItemRepository itemRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;


    public ShoppingListService(
            ShoppingListRepository shoppingListRepository,
            ItemRepository itemRepository,
            GroupRepository groupRepository,
            UserRepository userRepository) {
        this.shoppingListRepository = shoppingListRepository;
        this.itemRepository = itemRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    /**
     * Saves or updates a shopping list.
     * @param shoppingList The list to save.
     * @return The saved list.
     */
    public ShoppingList save(ShoppingList shoppingList) {
        return shoppingListRepository.save(shoppingList);
    }

    /**
     * Retrieves all shopping lists.
     * @return A list of all shopping lists.
     */
    public List<ShoppingList> getAll() {
        return shoppingListRepository.findAll();
    }

    /**
     * Retrieves a shopping list by its ID.
     * @param id The ID of the list.
     * @return The found list, or {@code null}.
     */
    public ShoppingList getById(Long id) {
        return shoppingListRepository.findById(id).orElse(null);
    }


    public List<ShoppingList> getOwnLists() {
        return shoppingListRepository.findAll().stream()
                .filter(ShoppingList::getIsPrivate)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all private shopping lists for a given user.
     * @param userEmail The email of the owner.
     * @return A list of private shopping lists.
     */
    public List<ShoppingList> getOwnLists(String userEmail) {
        return shoppingListRepository.findByOwnerEmailAndIsPrivateTrue(userEmail);
    }

    /**
     * Retrieves all shared (non-private) shopping lists.
     * @return A list of shared shopping lists.
     */
    public List<ShoppingList> getSharedLists() {
        return shoppingListRepository.findAll().stream()
                .filter(list -> !list.getIsPrivate())
                .collect(Collectors.toList());
    }

    /**
     * Creates a new shopping list along with its initial items from a DTO.
     * @param dto The DTO containing list and item data.
     * @return The newly created and persisted {@link ShoppingList}.
     */
    @Transactional
    public ShoppingList createWithItems(ShoppingListDto dto) {
        System.out.println("🔧 Starte Erstellung der Liste mit Titel: " + dto.getTitle());

        ShoppingList list = new ShoppingList();
        list.setTitle(dto.getTitle());

        // Optional: Besitzer setzen (private Liste)
        if (dto.getOwnerEmail() != null) {
            list.setOwnerEmail(dto.getOwnerEmail());
        }

        // 👥 Optional: Gruppe zuweisen (gemeinsame Liste)
        if (dto.getGroupId() != null) {
            groupRepository.findById(dto.getGroupId()).ifPresentOrElse(
                    list::setGroup,
                    () -> System.out.println("⚠️ Keine Gruppe mit ID " + dto.getGroupId() + " gefunden.")
            );
        }

        // Sichtbarkeit wie vom Frontend übergeben
        list.setIsPrivate(Boolean.TRUE.equals(dto.getIsPrivate()));

        // Produkte hinzufügen
        List<Item> items = new ArrayList<>();
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (var itemDto : dto.getItems()) {
                Item item = new Item();
                item.setName(itemDto.getName());
                item.setQuantity(itemDto.getQuantity());
                item.setStatus(itemDto.getStatus() != null
                        ? ItemStatus.valueOf(itemDto.getStatus().toUpperCase())
                        : ItemStatus.OFFEN);

                if (itemDto.getAddedById() != null) {
                    userRepository.findById(itemDto.getAddedById()).ifPresent(item::setAddedBy);
                }

                if (itemDto.getBoughtById() != null) {
                    userRepository.findById(itemDto.getBoughtById()).ifPresent(item::setBoughtBy);
                }

                item.setShoppingList(list);
                items.add(item);
            }
        }

        list.setItems(items);
        ShoppingList savedList = shoppingListRepository.saveAndFlush(list);
        System.out.println("✅ Liste inkl. Items gespeichert. ID: " + savedList.getId());
        return savedList;
    }

    /**
     * Retrieves all lists (private and shared) relevant to a user, with their items eagerly fetched.
     * @param userEmail The email of the user.
     * @return A list of relevant {@link ShoppingList}s.
     */
    public List<ShoppingList> getListsWithItems(String userEmail) {
        var user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return List.of();
        }

        return shoppingListRepository.findAllWithItems().stream()
                .filter(list -> {
                    if (list.getIsPrivate()) {
                        return userEmail.equals(list.getOwnerEmail());
                    } else {
                        return list.getGroup() != null &&
                                user.getGroup() != null &&
                                list.getGroup().getId().equals(user.getGroup().getId());
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Deletes a shopping list and its associated items.
     * @param id The ID of the list to delete.
     */
    @Transactional
    public void deleteById(Long id) {
        System.out.println("Versuche Liste " + id + " zu löschen");

        shoppingListRepository.findById(id).ifPresentOrElse(list -> {
            // Wichtig: Beziehung zur Gruppe trennen
            if (list.getGroup() != null) {
                list.getGroup().getShoppingLists().remove(list);
            }

            // Hibernate kümmert sich durch Cascade + Orphan Removal um Items
            shoppingListRepository.delete(list);
            shoppingListRepository.flush();

            System.out.println("Liste gelöscht aus DB: " + id);
            boolean stillExists = shoppingListRepository.findById(id).isPresent();
            System.out.println("Liste noch vorhanden? " + stillExists);
        }, () -> {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Liste nicht gefunden");
        });
    }

    /**
     * Suggests frequently bought items for a user that are not currently on any of their open lists.
     * @param user The user for whom to generate suggestions.
     * @return A list of up to 3 item name suggestions.
     */
    public List<String> getFrequentItemSuggestions(User user) {
        String userEmail = user.getEmail();

        // 1. Hole ALLE privaten Listen des Nutzers
        List<ShoppingList> allPrivateLists = this.getOwnLists(userEmail);

        // 2. Sammle die Namen ALLER Artikel auf diesen Listen in einem Set
        Set<String> itemsCurrentlyOnLists = allPrivateLists.stream()
                .flatMap(list -> list.getItems().stream())
                .filter(item -> item.getStatus() == ItemStatus.OFFEN) // <-- DAS IST DIE NEUE ZEILE!
                .map(item -> item.getName().toLowerCase())
                .collect(Collectors.toSet());

        // 3. Hole die häufigsten Artikelnamen aus der Datenbank
        List<String> frequentItems = itemRepository.findFrequentlyBoughtItemNamesByOwnerEmail(userEmail);

        // 4. Filtere die Vorschläge: Behalte nur die, die NICHT auf irgendeiner der Listen sind
        return frequentItems.stream()
                .filter(itemName -> !itemsCurrentlyOnLists.contains(itemName.toLowerCase()))
                .limit(3) // Begrenze auf die Top 3 Vorschläge
                .collect(Collectors.toList());
    }

    /**
     * Adds a batch of items to an existing shopping list.
     * @param dto The DTO containing the list ID and the items to add.
     * @param currentUser The authenticated user adding the items.
     */
    @Transactional
    public void addItemsToList(AddItemsRequestDto dto, User currentUser) {
        ShoppingList list = shoppingListRepository.findById(dto.getListId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Einkaufsliste nicht gefunden"));

        // Iteriere jetzt durch die Liste von Item-DTOs
        List<Item> newItems = dto.getItems().stream()
                .map(itemDto -> {
                    Item item = new Item();
                    item.setName(itemDto.getName());
                    // Verwende die Menge aus dem DTO
                    item.setQuantity(itemDto.getQuantity());
                    item.setStatus(ItemStatus.OFFEN);
                    item.setAddedBy(currentUser);
                    item.setShoppingList(list);
                    return item;
                })
                .collect(Collectors.toList());

        itemRepository.saveAll(newItems);
        System.out.println("✅ " + newItems.size() + " Artikel zur Liste ID " + dto.getListId() + " hinzugefügt.");
    }
}
