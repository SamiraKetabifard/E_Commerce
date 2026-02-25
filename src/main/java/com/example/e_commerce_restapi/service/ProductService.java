package com.example.e_commerce_restapi.service;

import com.example.e_commerce_restapi.dto.request.ProductRequest;
import com.example.e_commerce_restapi.dto.response.ProductResponse;
import java.util.List;

public interface ProductService {

    List<ProductResponse> getAll();
    ProductResponse create(ProductRequest request , Long categoryId);
    ProductResponse update(Long id, ProductRequest request);
    void delete(Long id);
}
