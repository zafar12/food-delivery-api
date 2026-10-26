package com.fooddelivery.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fooddelivery.entity.Restaurant;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByOwnerId(Long ownerId);
}