package com.fooddelivery.dto;

import jakarta.validation.constraints.NotBlank;

public class RestaurantDto {

    public static class Request {
        @NotBlank(message = "Restaurant name is required")
        private String name;
        private String description;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public record Response(
        Long id,
        String name,
        String description,
        String ownerEmail
    ) {}
}