package com.withademm.stores.config;

import com.withademm.stores.entity.Category;
import com.withademm.stores.entity.Role;
import com.withademm.stores.entity.User;
import com.withademm.stores.repository.CategoryRepository;
import com.withademm.stores.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      CategoryRepository categoryRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Seed 4 Test Users
        createUserIfNotFound("Sara Customer", "customer@stores.com", "Pass123!", Role.ROLE_CUSTOMER);
        createUserIfNotFound("John Seller", "seller@stores.com", "Pass123!", Role.ROLE_SELLER);
        createUserIfNotFound("Mike Manager", "manager@stores.com", "Pass123!", Role.ROLE_MANAGER);
        createUserIfNotFound("Alex Admin", "admin@stores.com", "Pass123!", Role.ROLE_ADMIN);

        // 2. Seed Default Category (Electronics -> ID 1)
        if (categoryRepository.count() == 0) {
            Category electronics = new Category("Electronics");
            categoryRepository.save(electronics);
            System.out.println("✅ Seeded Category: Electronics (ID: " + electronics.getId() + ")");
        }
    }

    private void createUserIfNotFound(String name, String email, String rawPassword, Role role) {
        if (!userRepository.existsByEmail(email)) {
            User user = new User(name, email, passwordEncoder.encode(rawPassword), role);
            userRepository.save(user);
            System.out.println("✅ Seeded User: " + email + " with Role: " + role);
        }
    }
}