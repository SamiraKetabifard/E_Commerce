package com.example.e_commerce_restapi.service;

import com.example.e_commerce_restapi.entity.Category;
import com.example.e_commerce_restapi.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category create(String name){
        Category category= new Category();
        category.setName(name);
        Category savedCategory= categoryRepository.save(category);
        return savedCategory;
    }

    public List<Category> getAll(){
        List<Category> categories = categoryRepository.findAll();
        return categories;
    }
}
