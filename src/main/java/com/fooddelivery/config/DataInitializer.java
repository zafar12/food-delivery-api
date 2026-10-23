package com.fooddelivery.config;

import com.fooddelivery.model.User;
import com.fooddelivery.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@toptal.com";
            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = new User();
                        admin.setEmail(adminEmail);
                        admin.setPassword(passwordEncoder.encode("admin123"));
                        admin.setRole(User.Role.ADMINISTRATOR);
                        admin.setBlocked(false);
                        admin.setImmutableAdmin(false);
                userRepository.save(admin);
            }
        };
    }
}