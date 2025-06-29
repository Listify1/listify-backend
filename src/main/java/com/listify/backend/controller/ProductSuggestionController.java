package com.listify.backend.controller;

import com.listify.backend.model.ProductSuggestion;
import com.listify.backend.service.ProductSuggestionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for providing product suggestions to the frontend.
 * <p>
 * This class exposes a public REST endpoint under the {@code /api/suggestions} path
 * to fetch a list of common products, which can be optionally filtered by a search query.
 * The endpoint is configured with a broad {@code @CrossOrigin} policy for wide accessibility.
 */
@RestController
@RequestMapping("/api/suggestions")
@CrossOrigin(origins = "*")
public class ProductSuggestionController {

    private final ProductSuggestionService service;

    /**
     * Constructs the controller and injects the required {@link ProductSuggestionService}.
     *
     * @param service The service responsible for fetching product suggestions.
     */
    public ProductSuggestionController(ProductSuggestionService service) {
        this.service = service;
    }

    /**
     * Retrieves a list of product suggestions, optionally filtered by a search query.
     * <p>
     * This endpoint responds to GET requests and accepts an optional query parameter 'q'.
     * If 'q' is provided, the returned list will contain suggestions matching the query.
     * If 'q' is omitted or empty, it may return a default or complete list of suggestions.
     *
     * @param q The search query string used to filter suggestions. Defaults to an empty string.
     * @return A list of {@link ProductSuggestion} objects that match the search query.
     */
    @GetMapping
    public List<ProductSuggestion> getSuggestions(@RequestParam(defaultValue = "") String q) {
        return service.getSuggestions(q);
    }
}