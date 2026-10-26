package com.fooddelivery.service;

import com.fooddelivery.dto.MealDto;
import com.fooddelivery.dto.OrderDto;
import com.fooddelivery.entity.Coupon;
import com.fooddelivery.entity.Meal;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.Restaurant;
import com.fooddelivery.entity.User;
import com.fooddelivery.repository.MealRepository;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class OrderService {

	private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MealRepository mealRepository;
    private final CouponService couponService; 

    public OrderService(OrderRepository orderRepository, UserRepository userRepository,
                        RestaurantRepository restaurantRepository, MealRepository mealRepository,
                        CouponService couponService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.mealRepository = mealRepository;
        this.couponService = couponService;
    }
    
    public Page<OrderDto.Response> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::mapToResponse);
    }

    public OrderDto.Response getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + id));
        
        validateOrderAccess(order);
        return mapToResponse(order);
    }

    @Transactional
    public OrderDto.Response placeOrder(OrderDto.Request request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        User customer = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with email: " + email));

        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with ID: " + request.getRestaurantId()));

        List<Meal> meals = mealRepository.findAllById(request.getMealIds());
        if (meals.isEmpty()) {
            throw new IllegalArgumentException("At least one valid meal must be selected");
        }

        for (Meal meal : meals) {
            if (!meal.getRestaurant().getId().equals(restaurant.getId())) {
                throw new IllegalArgumentException("Meal ID " + meal.getId() + " does not belong to restaurant " + restaurant.getId());
            }
        }

        // 1. Calculate raw subtotal
        Double subtotal = meals.stream()
                .mapToDouble(Meal::getPrice)
                .sum();

        // 2. Validate coupon and calculate discount amount
        Coupon coupon = couponService.validateAndGetCoupon(request.getCouponCode(), subtotal);
        Double discountAmount = couponService.calculateDiscount(subtotal, coupon);

        // 3. Final total price calculation
        Double totalPrice = Math.max(0.0, subtotal - discountAmount);

        Order order = new Order();
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setMeals(meals);
        // Note: You can store discountAmount/subtotal fields on your Order entity if desired, 
        // or just persist the final totalPrice. Here we store totalPrice.
        order.setTotalPrice(totalPrice);
        order.setStatus(Order.OrderStatus.PLACED);

        Order saved = orderRepository.save(order);
        return mapToResponse(saved, subtotal, discountAmount);
    }

    @Transactional
    public OrderDto.Response updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderId));

        validateOrderAccess(order);

        order.setStatus(newStatus);
        return mapToResponse(orderRepository.save(order));
    }

    private void validateOrderAccess(Order order) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Current authenticated user not found"));

        if (currentUser.getRole() == User.Role.RESTAURANT_OWNER) {
            if (order.getRestaurant().getOwner() == null || 
                !order.getRestaurant().getOwner().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You do not own the restaurant associated with this order.");
            }
        } else if (currentUser.getRole() == User.Role.CUSTOMER) {
            if (order.getCustomer() == null || 
                !order.getCustomer().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You are not authorized to view or modify this order.");
            }
        }
    }
    
    private OrderDto.Response mapToResponse(Order order) {
        // Fallback for general lookups where subtotal/discount aren't independently tracked; for flow order is not being placed 
        Double subtotal = order.getTotalPrice();
        return mapToResponse(order, subtotal, 0.0);
    }

    private OrderDto.Response mapToResponse(Order order, Double subtotal, Double discountAmount) {
        List<MealDto.Response> mealResponses = order.getMeals().stream()
                .map(m -> new MealDto.Response(
                        m.getId(), 
                        m.getName(), 
                        m.getDescription(), 
                        m.getPrice(), 
                        m.getRestaurant() != null ? m.getRestaurant().getId() : null
                ))
                .toList();

        return new OrderDto.Response(
                order.getId(),
                order.getCustomer() != null ? order.getCustomer().getEmail() : null,
                order.getRestaurant() != null ? order.getRestaurant().getName() : null,
                order.getStatus(),
                subtotal,
                discountAmount,
                order.getTotalPrice(),
                mealResponses
        );
    }
}