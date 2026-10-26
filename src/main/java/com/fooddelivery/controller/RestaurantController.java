package com.fooddelivery.controller;

import com.fooddelivery.dto.RestaurantDto;
import com.fooddelivery.service.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping
    public ResponseEntity<Page<RestaurantDto.Response>> getAllRestaurants(
            @RequestParam(name="page", defaultValue = "0") int page,
            @RequestParam(name="size",defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return ResponseEntity.ok(restaurantService.getAllRestaurants(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDto.Response> getRestaurantById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    @PostMapping
    public ResponseEntity<RestaurantDto.Response> createRestaurant(@Valid @RequestBody RestaurantDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.createRestaurant(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantDto.Response> updateRestaurant(
            @PathVariable("id") Long id,
            @Valid @RequestBody RestaurantDto.Request request) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable("id") Long id) {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.noContent().build();
    }
}