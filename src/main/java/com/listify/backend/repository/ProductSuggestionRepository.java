package com.listify.backend.repository;

import com.listify.backend.model.ProductSuggestion;
import com.listify.backend.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link ProductSuggestion} entity, using a {@link UUID} as the primary key.
 *
 * @author Listify Team
 * @version 1.0
 * @see ProductSuggestion
 */
@Repository
public interface ProductSuggestionRepository extends JpaRepository<ProductSuggestion, UUID> {

    /**
     * Finds product suggestions by name, ignoring case.
     *
     * @param name The name or part of the name to search for.
     * @return A list of matching {@link ProductSuggestion}s.
     */
    List<ProductSuggestion> findByNameContainingIgnoreCase(String name);

    /**
     * Finds all product suggestions submitted by a specific creator email.
     *
     * @param email The email of the creator.
     * @return A list of {@link ProductSuggestion}s from that creator.
     */
    List<ProductSuggestion> findByCreatorEmail(String email);

    /**
     * Deletes all product suggestions created by a user with the specified email.
     *
     * @param email The email of the creator whose suggestions should be deleted.
     */
    void deleteAllByCreatorEmail(@Param("email") String email);


}
