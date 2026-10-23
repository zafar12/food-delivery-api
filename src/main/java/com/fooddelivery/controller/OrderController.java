package com.fooddelivery.controller;


import com.fooddelivery.model.Order;
import com.fooddelivery.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * REST Controller for placing orders and managing order status updates.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Order> placeOrder(@RequestBody OrderRequest request, Principal principal) {
        Order order = orderService.placeOrder(
                principal.getName(),
                request.getRestaurantId(),
                request.getMealIds(),
                request.getCustomTip(),
                request.getCouponCode()
        );
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getOrders(Principal principal) {
        return ResponseEntity.ok(orderService.getOrdersForUser(principal.getName()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable("id") Long id, @RequestParam Order.OrderStatus status, Principal principal) {
        Order order = orderService.updateOrderStatus(id, status, principal.getName());
        return ResponseEntity.ok(order);
    }

    public static class OrderRequest {
        private Long restaurantId;
        private List<Long> mealIds;
        private Double customTip;
        private String couponCode;

        public Long getRestaurantId() { return restaurantId; }
        public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
        public List<Long> getMealIds() { return mealIds; }
        public void setMealIds(List<Long> mealIds) { this.mealIds = mealIds; }
        public Double getCustomTip() { return customTip; }
        public void setCustomTip(Double customTip) { this.customTip = customTip; }
        public String getCouponCode() { return couponCode; }
        public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    }
}