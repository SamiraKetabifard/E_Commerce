package com.example.e_commerce_restapi.service;

import com.example.e_commerce_restapi.dto.request.ProductRequest;
import com.example.e_commerce_restapi.dto.response.ProductResponse;
import com.example.e_commerce_restapi.entity.Category;
import com.example.e_commerce_restapi.entity.Product;
import com.example.e_commerce_restapi.repository.CategoryRepository;
import com.example.e_commerce_restapi.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<ProductResponse> getAll() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ProductResponse create(ProductRequest request, Long categoryId) {
        Product product = modelMapper.map(request, Product.class);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException(
                        "Category not found with id: " + categoryId
                ));

        product.setCategory(category);
        Product saveProduct = productRepository.save(product);
        return mapToResponse(saveProduct);
    }

    @Override
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Product not found with id: " + id
                ));

        modelMapper.map(request, product);

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException(
                            "Category not found with id: " + request.getCategoryId()
                    ));
            product.setCategory(category);
        }

        Product updated = productRepository.save(product);
        return mapToResponse(updated);
    }

    @Override
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Product not found with id: " + id
                ));
        productRepository.delete(product);
    }

    private ProductResponse mapToResponse(Product product) {
        ProductResponse response = modelMapper.map(product, ProductResponse.class);
        response.setCategory(product.getCategory().getName());
        return response;
    }
}