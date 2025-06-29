package com.listify.backend.dto;

import lombok.Data;
import java.util.List;

/**
 * Represents a request payload for adding a batch of items to a specific shopping list.
 * <p>
 * This Data Transfer Object (DTO) is used in controller endpoints to encapsulate all the
 * necessary information for a bulk item addition operation.
 */
@Data
public class AddItemsRequestDto {

    /**
     * The unique identifier of the shopping list to which the items will be added.
     */
    private Long listId;

    /**
     * A list of items to be added to the shopping list.
     * Each item in the list is represented by an {@link ItemToAddDto}.
     */
    private List<ItemToAddDto> items;

    /**
     * A nested static class representing the data for a single item to be added.
     * <p>
     * This class contains the essential properties required to create a new item.
     */
    @Data
    public static class ItemToAddDto {
        /**
         * The name of the item (e.g., "Milk").
         */
        private String name;

        /**
         * The quantity of the item (e.g., 2).
         */
        private int quantity;
    }
}