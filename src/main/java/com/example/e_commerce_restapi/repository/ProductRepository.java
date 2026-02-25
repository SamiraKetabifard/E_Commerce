package com.example.e_commerce_restapi.repository;

import com.example.e_commerce_restapi.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
