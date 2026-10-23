package com.fooddelivery.service;

import com.fooddelivery.model.Meal;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.model.User;
import com.fooddelivery.repository.MealRepository;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service handling menu and meal operations for restaurants.
 */
@Service
public class MealService {

    private final MealRepository mealRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    public MealService(MealRepository mealRepository, RestaurantRepository restaurantRepository, UserRepository userRepository) {
        this.mealRepository = mealRepository;
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
    }

    /**
     * Adds a new meal to a specific restaurant's menu.
     */
    @Transactional
    public Meal createMeal(Long restaurantId, String name, String description, Double price, String userEmail) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != User.Role.ADMINISTRATOR && !restaurant.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to add meals to this restaurant");
        }

        Meal meal = new Meal();
        meal.setName(name);
        meal.setDescription(description);
        meal.setPrice(price);
        meal.setRestaurant(restaurant);

        return mealRepository.save(meal);
    }

    /**
     * Retrieves all meals belonging to a specific restaurant.
     */
    public List<Meal> getMealsByRestaurant(Long restaurantId) {
        return mealRepository.findByRestaurantId(restaurantId);
    }

    /**
     * Updates an existing meal.
     */
    @Transactional
    public Meal updateMeal(Long mealId, String name, String description, Double price, String userEmail) {
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new RuntimeException("Meal not found"));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != User.Role.ADMINISTRATOR && !meal.getRestaurant().getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to update this meal");
        }

        meal.setName(name);
        meal.setDescription(description);
        meal.setPrice(price);

        return mealRepository.save(meal);
    }

    /**
     * Deletes a meal from a restaurant menu.
     */
    @Transactional
    public void deleteMeal(Long mealId, String userEmail) {
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new RuntimeException("Meal not found"));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != User.Role.ADMINISTRATOR && !meal.getRestaurant().getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to delete this meal");
        }

        mealRepository.delete(meal);
    }
}