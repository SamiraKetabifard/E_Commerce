package com.example.e_commerce_restapi.repository;

import com.example.e_commerce_restapi.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
