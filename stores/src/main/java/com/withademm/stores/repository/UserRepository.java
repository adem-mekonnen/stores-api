package com.withademm.stores.repository;

import com.withademm.stores.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Used by Spring Security to look up a user by email during login
    Optional<User> findByEmail(String email);

    // Used to check if an email is already registered
    boolean existsByEmail(String email);
}