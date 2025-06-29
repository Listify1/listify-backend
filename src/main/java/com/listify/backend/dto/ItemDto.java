package com.listify.backend.dto;

import lombok.Data;

/**
 * Represents a request payload for creating or updating a shopping list item.
 * <p>
 * This Data Transfer Object (DTO) encapsulates all the necessary information
 * required to create a new {@link com.listify.backend.model.Item} entity or to update an existing one.
 * It is used primarily in controller endpoints to receive data from the client.
 */
@Data
public class ItemDto {

    /**
     * The name of the item (e.g., "Milk", "Bread").
     */
    private String name;

    /**
     * The quantity of the item to be purchased.
     */
    private int quantity;

    /**
     * The current status of the item, typically represented as a string
     * that can be converted to the {@link com.listify.backend.model.enums.ItemStatus} enum
     * (e.g., "OFFEN", "GEKAUFT").
     */
    private String status;

    /**
     * The unique identifier of the user who added the item to the list.
     * This may be null if the creator is determined from the security context.
     */
    private Long addedById;

    /**
     * The unique identifier of the user who marked the item as bought.
     * This is typically null when the item is first created.
     */
    private Long boughtById;

    /**
     * The unique identifier of the shopping list to which this item belongs.
     * This field is mandatory for creating a new item.
     */
    private Long shoppingListId;
}