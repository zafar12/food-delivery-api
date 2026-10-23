package com.fooddelivery.controller;

import com.fooddelivery.model.Restaurant;
import com.fooddelivery.service.RestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * REST Controller for browsing and managing restaurants.
 */
@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping
    public ResponseEntity<List<Restaurant>> getAllRestaurants() {
        return ResponseEntity.ok(restaurantService.getAllRestaurants());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMINISTRATOR')")
    public ResponseEntity<Restaurant> createRestaurant(@RequestBody RestaurantRequest request, Principal principal) {
        Restaurant restaurant = restaurantService.createRestaurant(request.getName(), request.getDescription(), principal.getName());
        return ResponseEntity.ok(restaurant);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMINISTRATOR')")
    public ResponseEntity<Restaurant> updateRestaurant(@PathVariable("id") Long id, @RequestBody RestaurantRequest request, Principal principal) {
        Restaurant updated = restaurantService.updateRestaurant(id, request.getName(), request.getDescription(), principal.getName());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMINISTRATOR')")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable("id") Long id, Principal principal) {
        restaurantService.deleteRestaurant(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    // DTO for Request Payload
    public static class RestaurantRequest {
        private String name;
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
