package com.fooddelivery.service;

import com.fooddelivery.dto.MealDto;
import com.fooddelivery.entity.Meal;
import com.fooddelivery.entity.Restaurant;
import com.fooddelivery.entity.User;
import com.fooddelivery.repository.MealRepository;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true) // Default all methods to read-only for optimal performance
public class MealService {

    private final MealRepository mealRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    public MealService(MealRepository mealRepository, 
                       RestaurantRepository restaurantRepository, 
                       UserRepository userRepository) {
        this.mealRepository = mealRepository;
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
    }

    public Page<MealDto.Response> getAllMeals(Pageable pageable) {
        return mealRepository.findAll(pageable).map(this::mapToResponse);
    }

    public Page<MealDto.Response> getMealsByRestaurant(Long restaurantId, Pageable pageable) {
        // Verify restaurant exists first
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new EntityNotFoundException("Restaurant not found with ID: " + restaurantId);
        }
        return mealRepository.findByRestaurantId(restaurantId, pageable).map(this::mapToResponse);
    }

    public MealDto.Response getMealById(Long id) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Meal not found with ID: " + id));
        return mapToResponse(meal);
    }

    @Transactional
    public MealDto.Response createMeal(MealDto.Request request) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with ID: " + request.getRestaurantId()));

        // Object-level authorization check: Ensure restaurant owner owns this restaurant
        validateRestaurantOwnership(restaurant);

        Meal meal = new Meal();
        meal.setName(request.getName());
        meal.setDescription(request.getDescription());
        meal.setPrice(request.getPrice());
        meal.setRestaurant(restaurant);

        Meal saved = mealRepository.save(meal);
        return mapToResponse(saved);
    }

    @Transactional
    public MealDto.Response updateMeal(Long id, MealDto.Request request) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Meal not found with ID: " + id));

        // Object-level authorization check: Ensure owner owns the restaurant linked to this meal
        validateRestaurantOwnership(meal.getRestaurant());

        // If restaurant ID changed in the request, verify the new restaurant exists and check ownership
        if (!meal.getRestaurant().getId().equals(request.getRestaurantId())) {
            Restaurant newRestaurant = restaurantRepository.findById(request.getRestaurantId())
                    .orElseThrow(() -> new EntityNotFoundException("New restaurant not found with ID: " + request.getRestaurantId()));
            validateRestaurantOwnership(newRestaurant);
            meal.setRestaurant(newRestaurant);
        }

        meal.setName(request.getName());
        meal.setDescription(request.getDescription());
        meal.setPrice(request.getPrice());

        Meal updated = mealRepository.save(meal);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteMeal(Long id) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Meal not found with ID: " + id));

        // Object-level authorization check
        validateRestaurantOwnership(meal.getRestaurant());

        mealRepository.delete(meal);
    }

    // Helper method for ownership verification (IDOR protection)
    private void validateRestaurantOwnership(Restaurant restaurant) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Current authenticated user not found"));

        if (currentUser.getRole() == User.Role.RESTAURANT_OWNER) {
            if (restaurant.getOwner() == null || !restaurant.getOwner().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You do not own the restaurant associated with this meal.");
            }
        }
    }

    // Helper mapper method
    private MealDto.Response mapToResponse(Meal meal) {
        return new MealDto.Response(
                meal.getId(),
                meal.getName(),
                meal.getDescription(),
                meal.getPrice(),
                meal.getRestaurant() != null ? meal.getRestaurant().getId() : null
        );
    }
}