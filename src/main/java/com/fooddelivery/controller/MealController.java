package com.fooddelivery.controller;

import com.fooddelivery.dto.MealDto;
import com.fooddelivery.service.MealService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meals")
public class MealController {

    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    @GetMapping
    public ResponseEntity<Page<MealDto.Response>> getAllMeals(
            @RequestParam(name="page", defaultValue = "0") int page,
            @RequestParam(name="size", defaultValue = "10") int size,
            @RequestParam(name="restaurantId", required = false) Long restaurantId) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<MealDto.Response> mealPage = (restaurantId != null)
                ? mealService.getMealsByRestaurant(restaurantId, pageable)
                : mealService.getAllMeals(pageable);

        return ResponseEntity.ok(mealPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MealDto.Response> getMealById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(mealService.getMealById(id));
    }

    @PostMapping
    public ResponseEntity<MealDto.Response> createMeal(@Valid @RequestBody MealDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mealService.createMeal(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MealDto.Response> updateMeal(
            @PathVariable("id") Long id,
            @Valid @RequestBody MealDto.Request request) {
        return ResponseEntity.ok(mealService.updateMeal(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeal(@PathVariable("id") Long id) {
        mealService.deleteMeal(id);
        return ResponseEntity.noContent().build();
    }
}