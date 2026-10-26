package com.fooddelivery.service;

import com.fooddelivery.dto.UserDto;
import com.fooddelivery.entity.User;
import com.fooddelivery.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<UserDto.Response> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional
    public UserDto.Response createUser(UserDto.Request request) {
        
    	if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Encrypt password
        user.setRole(request.getRole());
        user.setBlocked(false);
        user.setImmutableAdmin(false);

        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    @Transactional
    public UserDto.Response toggleBlockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));

        if (user.isImmutableAdmin()) {
            throw new IllegalStateException("Cannot block built-in administrator account");
        }

        user.setBlocked(!user.isBlocked());
        User updated = userRepository.save(user);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));

        if (user.isImmutableAdmin()) {
            throw new IllegalStateException("Cannot delete built-in administrator account");
        }

        userRepository.delete(user);
    }

    private UserDto.Response mapToResponse(User user) {
        return new UserDto.Response(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isImmutableAdmin(),
                user.isBlocked()
        );
    }
}