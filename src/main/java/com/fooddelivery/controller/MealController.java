package com.fooddelivery.controller;

import com.fooddelivery.model.Meal;
import com.fooddelivery.service.MealService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * REST Controller for managing restaurant menu meals.
 */
@RestController
@RequestMapping("/api/restaurants/{restaurantId}/meals")
public class MealController {

    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    @GetMapping
    public ResponseEntity<List<Meal>> getMeals(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(mealService.getMealsByRestaurant(restaurantId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMINISTRATOR')")
    public ResponseEntity<Meal> createMeal(@PathVariable Long restaurantId, @RequestBody MealRequest request, Principal principal) {
        Meal meal = mealService.createMeal(restaurantId, request.getName(), request.getDescription(), request.getPrice(), principal.getName());
        return ResponseEntity.ok(meal);
    }

    @PutMapping("/{mealId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMINISTRATOR')")
    public ResponseEntity<Meal> updateMeal(@PathVariable Long mealId, @RequestBody MealRequest request, Principal principal) {
        Meal meal = mealService.updateMeal(mealId, request.getName(), request.getDescription(), request.getPrice(), principal.getName());
        return ResponseEntity.ok(meal);
    }

    @DeleteMapping("/{mealId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMINISTRATOR')")
    public ResponseEntity<Void> deleteMeal(@PathVariable Long mealId, Principal principal) {
        mealService.deleteMeal(mealId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    public static class MealRequest {
        private String name;
        private String description;
        private Double price;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
    }
}