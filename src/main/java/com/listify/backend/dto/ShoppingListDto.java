package com.listify.backend.dto;

import java.util.List;

/**
 * Represents a request payload for creating a new shopping list.
 * <p>
 * This Data Transfer Object (DTO) encapsulates all the necessary information for
 * creating a {@link com.listify.backend.model.ShoppingList}, including its metadata
 * (like title and privacy status) and a list of initial items.
 */
public class ShoppingListDto {

    /**
     * The title of the shopping list (e.g., "Weekly Groceries", "Hardware Store").
     */
    private String title;

    /**
     * The unique identifier of the group to which this list belongs.
     * This can be null if the list is marked as private.
     */
    private Long groupId;

    /**
     * A flag indicating whether the shopping list is private to the owner
     * ({@code true}) or shared with their group ({@code false}).
     */
    private Boolean isPrivate;

    /**
     * A list of initial items to be added to the shopping list upon its creation.
     */
    private List<ItemDto> items;

    /**
     * The email address of the user who owns this shopping list.
     * This is used to assign ownership upon creation.
     */
    private String ownerEmail;

    /**
     * Gets the title of the shopping list.
     *
     * @return the title string.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the ID of the associated group.
     *
     * @return the group ID, or null if it's a private list.
     */
    public Long getGroupId() {
        return groupId;
    }

    /**
     * Checks if the shopping list is private.
     *
     * @return {@code true} if the list is private, {@code false} otherwise.
     */
    public Boolean getIsPrivate() {
        return isPrivate;
    }

    /**
     * Gets the list of initial items.
     *
     * @return a list of {@link ItemDto} objects.
     */
    public List<ItemDto> getItems() {
        return items;
    }

    /**
     * Gets the email of the list's owner.
     *
     * @return the owner's email string.
     */
    public String getOwnerEmail() {
        return ownerEmail;
    }

    /**
     * Sets the title of the shopping list.
     *
     * @param title the new title string.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the ID of the associated group.
     *
     * @param groupId the group ID.
     */
    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    /**
     * Sets the privacy status of the shopping list.
     *
     * @param isPrivate {@code true} for a private list, {@code false} for a shared one.
     */
    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    /**
     * Sets the list of initial items.
     *
     * @param items a list of {@link ItemDto} objects.
     */
    public void setItems(List<ItemDto> items) {
        this.items = items;
    }

    /**
     * Sets the email of the list's owner.
     *
     * @param ownerEmail the owner's email string.
     */
    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
}