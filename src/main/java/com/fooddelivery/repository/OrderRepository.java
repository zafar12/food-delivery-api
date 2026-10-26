// OrderRepository.java
package com.fooddelivery.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fooddelivery.entity.Order;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByRestaurantId(Long restaurantId);
}