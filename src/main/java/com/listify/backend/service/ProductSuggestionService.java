package com.listify.backend.service;

import com.listify.backend.model.ProductSuggestion;
import com.listify.backend.repository.ProductSuggestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for managing {@link ProductSuggestion} entities.
 *
 * @author Listify Team
 * @version 1.0
 */
@Service
public class ProductSuggestionService {
    private final ProductSuggestionRepository repo;

    public ProductSuggestionService(ProductSuggestionRepository repo) {
        this.repo = repo;
    }

    /**
     * Retrieves suggestions based on a name filter, ignoring case.
     * @param filter The string to filter product names by.
     * @return A list of matching suggestions.
     */
    public List<ProductSuggestion> getSuggestions(String filter) {
        return repo.findByNameContainingIgnoreCase(filter);
    }

    /**
     * Retrieves all product suggestions.
     * @return A list of all suggestions.
     */
    public List<ProductSuggestion> getAll() {
        return repo.findAll();
    }

    /**
     * Finds matching product suggestion names for a given query string.
     * @param q The query string.
     * @return A list of suggestion names.
     */
    public List<String> findMatchingSuggestions(String q) {
        return repo.findByNameContainingIgnoreCase(q)
                .stream()
                .map(ProductSuggestion::getName)
                .toList();
    }

}
