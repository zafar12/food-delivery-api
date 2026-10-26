package com.fooddelivery.service;

import com.fooddelivery.dto.RestaurantDto;
import com.fooddelivery.entity.Restaurant;
import com.fooddelivery.entity.User;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    public RestaurantService(RestaurantRepository restaurantRepository, UserRepository userRepository) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
    }

    public Page<RestaurantDto.Response> getAllRestaurants(Pageable pageable) {
        return restaurantRepository.findAll(pageable).map(this::mapToResponse);
    }

    public RestaurantDto.Response getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with ID: " + id));
        return mapToResponse(restaurant);
    }

    @Transactional
    public RestaurantDto.Response createRestaurant(RestaurantDto.Request request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with email: " + email));

        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setOwner(owner);

        Restaurant saved = restaurantRepository.save(restaurant);
        return mapToResponse(saved);
    }

    @Transactional
    public RestaurantDto.Response updateRestaurant(Long id, RestaurantDto.Request request) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with ID: " + id));

        // Object-level authorization check (IDOR prevention)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with email: " + email));

        if (currentUser.getRole() == User.Role.RESTAURANT_OWNER) {
            if (!restaurant.getOwner().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You do not own this restaurant.");
            }
        }

        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());

        Restaurant updated = restaurantRepository.save(restaurant);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with ID: " + id));

        // Object-level authorization check (IDOR prevention)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with email: " + email));

        if (currentUser.getRole() == User.Role.RESTAURANT_OWNER) {
            if (!restaurant.getOwner().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You do not own this restaurant.");
            }
        }

        restaurantRepository.delete(restaurant);
    }

    private RestaurantDto.Response mapToResponse(Restaurant restaurant) {
        return new RestaurantDto.Response(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getDescription(),
                restaurant.getOwner() != null ? restaurant.getOwner().getEmail() : null
        );
    }
}