// MealRepository.java
package com.fooddelivery.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fooddelivery.entity.Meal;

import java.util.List;

public interface MealRepository extends JpaRepository<Meal, Long> {
    List<Meal> findByRestaurantId(Long restaurantId);
    Page<Meal> findByRestaurantId(Long restaurantId, Pageable pageable);;
}