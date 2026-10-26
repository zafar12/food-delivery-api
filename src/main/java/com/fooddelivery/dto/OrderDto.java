package com.fooddelivery.dto;

import com.fooddelivery.entity.Order;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class OrderDto {

    public static class Request {
        @NotNull(message = "Restaurant ID is required")
        private Long restaurantId;

        @NotEmpty(message = "Order must contain at least one meal ID")
        private List<Long> mealIds;

        private String couponCode; // Optional field for checkout coupon

        // Getters and Setters
        public Long getRestaurantId() { return restaurantId; }
        public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
        public List<Long> getMealIds() { return mealIds; }
        public void setMealIds(List<Long> mealIds) { this.mealIds = mealIds; }
        public String getCouponCode() { return couponCode; }
        public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    }

    public record Response(
        Long id,
        String customerEmail,
        String restaurantName,
        Order.OrderStatus status,
        Double subtotal,
        Double discountAmount,
        Double totalPrice,
        List<MealDto.Response> meals
    ) {}
}