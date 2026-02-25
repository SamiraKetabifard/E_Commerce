package com.example.e_commerce_restapi.repository;

import com.example.e_commerce_restapi.entity.Cart;
import com.example.e_commerce_restapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}
