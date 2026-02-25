package com.example.e_commerce_restapi.repository;

import com.example.e_commerce_restapi.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
