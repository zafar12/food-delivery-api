package com.fooddelivery.service;

import com.fooddelivery.model.*;
import com.fooddelivery.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles order placement, price calculation, coupon discounts, and strict status progression rules.
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final MealRepository mealRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, RestaurantRepository restaurantRepository,
                        MealRepository mealRepository, CouponRepository couponRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.restaurantRepository = restaurantRepository;
        this.mealRepository = mealRepository;
        this.couponRepository = couponRepository;
        this.userRepository = userRepository;
    }

    /**
     * Places a new order for a customer at a single restaurant with optional tips and coupons.
     */
    @Transactional
    public Order placeOrder(String customerEmail, Long restaurantId, List<Long> mealIds, Double customTip, String couponCode) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        List<Meal> meals = mealRepository.findAllById(mealIds);
        if (meals.isEmpty()) {
            throw new RuntimeException("Order must contain at least one meal");
        }

        // Validate single restaurant rule
        for (Meal meal : meals) {
            if (!meal.getRestaurant().getId().equals(restaurantId)) {
                throw new RuntimeException("All meals in an order must belong to the same restaurant");
            }
        }

        double mealsTotal = meals.stream().mapToDouble(Meal::getPrice).sum();
        double discount = 0.0;

        if (couponCode != null && !couponCode.isBlank()) {
            Coupon coupon = couponRepository.findByCode(couponCode)
                    .orElseThrow(() -> new RuntimeException("Invalid coupon code"));
            discount = (mealsTotal * coupon.getDiscountPercentage()) / 100.0;
        }

        double tip = customTip != null ? customTip : 0.0;
        double totalAmount = (mealsTotal - discount) + tip;

        Order order = new Order();
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setMeals(meals);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setCustomTipAmount(tip);
        order.setCouponCode(couponCode);
        order.setDiscountApplied(discount);
        order.setStatus(Order.OrderStatus.PLACED);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setStatus(Order.OrderStatus.PLACED);
        history.setChangedAt(LocalDateTime.now());

        order.getStatusHistories().add(history);
        return orderRepository.save(order);
    }

    /**
     * Updates an order's status following state machine rules and permission validations.
     */
    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        validateStateTransition(order, newStatus, user);

        order.setStatus(newStatus);
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setStatus(newStatus);
        history.setChangedAt(LocalDateTime.now());
        order.getStatusHistories().add(history);

        return orderRepository.save(order);
    }

    /**
     * Retrieves orders for a customer, restaurant owner, or admin.
     */
    public List<Order> getOrdersForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == User.Role.ADMINISTRATOR) {
            return orderRepository.findAll();
        } else if (user.getRole() == User.Role.CUSTOMER) {
            return orderRepository.findByCustomerId(user.getId());
        } else if (user.getRole() == User.Role.RESTAURANT_OWNER) {
            List<Restaurant> restaurants = restaurantRepository.findByOwnerId(user.getId());
            return restaurants.stream()
                    .flatMap(r -> orderRepository.findByRestaurantId(r.getId()).stream())
                    .distinct()
                    .toList();
        }
        return List.of();
    }

    /**
     * Validates permitted transitions between order statuses.
     */
    private void validateStateTransition(Order order, Order.OrderStatus newStatus, User user) {
        boolean isCustomer = user.getId().equals(order.getCustomer().getId());
        boolean isOwner = user.getId().equals(order.getRestaurant().getOwner().getId());
        boolean isAdmin = user.getRole() == User.Role.ADMINISTRATOR;

        Order.OrderStatus current = order.getStatus();

        if (newStatus == Order.OrderStatus.CANCELED) {
            if (!isCustomer && !isOwner && !isAdmin) throw new RuntimeException("Unauthorized to cancel order");
            if (current != Order.OrderStatus.PLACED) throw new RuntimeException("Order can only be canceled when PLACED");
        } else if (newStatus == Order.OrderStatus.PROCESSING) {
            if (!isOwner && !isAdmin) throw new RuntimeException("Only restaurant owner can process orders");
            if (current != Order.OrderStatus.PLACED) throw new RuntimeException("Invalid status flow");
        } else if (newStatus == Order.OrderStatus.IN_ROUTE) {
            if (!isOwner && !isAdmin) throw new RuntimeException("Only restaurant owner can mark in route");
            if (current != Order.OrderStatus.PROCESSING) throw new RuntimeException("Invalid status flow");
        } else if (newStatus == Order.OrderStatus.DELIVERED) {
            if (!isOwner && !isAdmin) throw new RuntimeException("Only restaurant owner can mark delivered");
            if (current != Order.OrderStatus.IN_ROUTE) throw new RuntimeException("Invalid status flow");
        } else if (newStatus == Order.OrderStatus.RECEIVED) {
            if (!isCustomer && !isAdmin) throw new RuntimeException("Only customer can mark received");
            if (current != Order.OrderStatus.DELIVERED) throw new RuntimeException("Order must be delivered first");
        }
    }
}