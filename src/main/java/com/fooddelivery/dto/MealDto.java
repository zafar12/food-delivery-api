package com.fooddelivery.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MealDto {

    public static class Request {
        @NotBlank(message = "Meal name is required")
        private String name;
        private String description;

        @NotNull(message = "Price is required")
        @Min(value = 0, message = "Price must be greater than or equal to 0")
        private Double price;

        @NotNull(message = "Restaurant ID is required")
        private Long restaurantId;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public Long getRestaurantId() { return restaurantId; }
        public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
    }

    public record Response(
        Long id,
        String name,
        String description,
        Double price,
        Long restaurantId
    ) {}
}