package com.fooddelivery.service;

import com.fooddelivery.model.Restaurant;
import com.fooddelivery.model.User;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service handling business logic for restaurant management.
 */
@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    public RestaurantService(RestaurantRepository restaurantRepository, UserRepository userRepository) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new restaurant owned by the specified restaurant owner or admin.
     */
    @Transactional
    public Restaurant createRestaurant(String name, String description, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setDescription(description);
        restaurant.setOwner(owner);

        return restaurantRepository.save(restaurant);
    }

    /**
     * Retrieves all restaurants available in the system.
     */
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    /**
     * Retrieves a restaurant by its unique identifier.
     */
    public Restaurant getRestaurantById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));
    }

    /**
     * Updates an existing restaurant's details.
     */
    @Transactional
    public Restaurant updateRestaurant(Long id, String name, String description, String userEmail) {
        Restaurant restaurant = getRestaurantById(id);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != User.Role.ADMINISTRATOR && !restaurant.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to update this restaurant");
        }

        restaurant.setName(name);
        restaurant.setDescription(description);
        return restaurantRepository.save(restaurant);
    }

    /**
     * Deletes a restaurant by ID.
     */
    @Transactional
    public void deleteRestaurant(Long id, String userEmail) {
        Restaurant restaurant = getRestaurantById(id);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != User.Role.ADMINISTRATOR && !restaurant.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to delete this restaurant");
        }

        restaurantRepository.delete(restaurant);
    }
}