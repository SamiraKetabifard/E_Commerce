package com.example.e_commerce_restapi.service;

import com.example.e_commerce_restapi.dto.request.ProductRequest;
import com.example.e_commerce_restapi.dto.response.ProductResponse;
import com.example.e_commerce_restapi.entity.Category;
import com.example.e_commerce_restapi.entity.Product;
import com.example.e_commerce_restapi.repository.CategoryRepository;
import com.example.e_commerce_restapi.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ProductServiceUnitTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category testCategory;
    private Product testProduct;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        testCategory = new Category();
        testCategory.setName("mobile");
        testCategory = categoryRepository.save(testCategory);

        testProduct = new Product();
        testProduct.setName("iPhone 14");
        testProduct.setDescription("Apple iPhone");
        testProduct.setPrice(new BigDecimal("999.99"));
        testProduct.setStock(10);
        testProduct.setCategory(testCategory);
        testProduct = productRepository.save(testProduct);

        productRequest = new ProductRequest();
        productRequest.setName("iPhone 14");
        productRequest.setDescription("Apple iPhone");
        productRequest.setPrice(new BigDecimal("999.99"));
        productRequest.setStock(10);
        productRequest.setCategoryId(testCategory.getId());
    }

    @Test
    void getAll_ShouldReturnListOfProducts() {
        Product product2 = new Product();
        product2.setName("MacBook Pro");
        product2.setDescription("Apple Laptop");
        product2.setPrice(new BigDecimal("1999.99"));
        product2.setStock(5);
        product2.setCategory(testCategory);
        productRepository.save(product2);

        List<ProductResponse> result = productService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ProductResponse::getName)
                .containsExactlyInAnyOrder("iPhone 14", "MacBook Pro");
    }
    @Test
    void create_WithValidData_ShouldSaveProduct() {
        ProductRequest newRequest = new ProductRequest();
        newRequest.setName("Samsung S23");
        newRequest.setDescription("Samsung Phone");
        newRequest.setPrice(new BigDecimal("899.99"));
        newRequest.setStock(15);
        newRequest.setCategoryId(testCategory.getId());

        ProductResponse result = productService.create(newRequest, testCategory.getId());

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Samsung S23");
        assertThat(result.getCategory()).isEqualTo("mobile");

        Product savedProduct = productRepository.findById(result.getId()).orElse(null);
        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("Samsung S23");
        assertThat(savedProduct.getCategory().getId()).isEqualTo(testCategory.getId());
    }
    @Test
    void create_WithInvalidCategory_ShouldThrowException() {
        assertThatThrownBy(() -> productService.create(productRequest, 999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void update_WithValidId_ShouldUpdateProduct() {
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("iPhone 15");
        updateRequest.setDescription("New iPhone");
        updateRequest.setPrice(new BigDecimal("1099.99"));
        updateRequest.setStock(5);
        updateRequest.setCategoryId(testCategory.getId());

        ProductResponse result = productService.update(testProduct.getId(), updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("iPhone 15");

        Product updatedProduct = productRepository.findById(testProduct.getId()).orElse(null);
        assertThat(updatedProduct).isNotNull();
        assertThat(updatedProduct.getName()).isEqualTo("iPhone 15");
        assertThat(updatedProduct.getPrice()).isEqualTo(new BigDecimal("1099.99"));
        assertThat(updatedProduct.getStock()).isEqualTo(5);
    }

    @Test
    void update_WithInvalidProductId_ShouldThrowException() {
        assertThatThrownBy(() -> productService.update(999L, productRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void update_WithNewCategory_ShouldUpdateCategory() {
        Category newCategory = new Category();
        newCategory.setName("tech");
        newCategory = categoryRepository.save(newCategory);

        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("iPhone 14");
        updateRequest.setCategoryId(newCategory.getId());

        productService.update(testProduct.getId(), updateRequest);

        Product updatedProduct = productRepository.findById(testProduct.getId()).orElse(null);
        assertThat(updatedProduct).isNotNull();
        assertThat(updatedProduct.getCategory().getId()).isEqualTo(newCategory.getId());
        assertThat(updatedProduct.getCategory().getName()).isEqualTo("tech");
    }

    @Test
    void delete_WithValidId_ShouldDeleteProduct() {
        productService.delete(testProduct.getId());

        assertThat(productRepository.findById(testProduct.getId())).isEmpty();
    }

    @Test
    void delete_WithInvalidId_ShouldThrowException() {
        assertThatThrownBy(() -> productService.delete(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void create_WithNullCategoryId_ShouldWork() {
        ProductRequest request = new ProductRequest();
        request.setName("Test Product");
        request.setDescription("Test Description");
        request.setPrice(new BigDecimal("99.99"));
        request.setStock(10);
        request.setCategoryId(testCategory.getId());

        ProductResponse result = productService.create(request, testCategory.getId());

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Product");
    }
}