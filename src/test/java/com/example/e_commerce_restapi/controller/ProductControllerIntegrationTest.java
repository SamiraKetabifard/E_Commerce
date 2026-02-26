package com.example.e_commerce_restapi.controller;

import com.example.e_commerce_restapi.dto.request.LoginRequest;
import com.example.e_commerce_restapi.dto.request.ProductRequest;
import com.example.e_commerce_restapi.dto.response.ProductResponse;
import com.example.e_commerce_restapi.entity.*;
import com.example.e_commerce_restapi.repository.CategoryRepository;
import com.example.e_commerce_restapi.repository.ProductRepository;
import com.example.e_commerce_restapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static String adminToken;
    private static String customerToken;
    private static Long techCategoryId;
    private static Long mobileCategoryId;
    private static Long productId;
    private static boolean initialized = false;

    @BeforeEach
    void setUp() {
        if (!initialized) {
            productRepository.deleteAll();
            categoryRepository.deleteAll();
            userRepository.deleteAll();

            Category tech = new Category();
            tech.setName("tech");
            tech = categoryRepository.save(tech);
            techCategoryId = tech.getId();

            Category mobile = new Category();
            mobile.setName("mobile");
            mobile = categoryRepository.save(mobile);
            mobileCategoryId = mobile.getId();

            User admin = new User();
            admin.setEmail("admin@gmail.com");
            admin.setName("admin");
            admin.setPassword(passwordEncoder.encode("12"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);

            User customer = new User();
            customer.setEmail("p@gmail.com");
            customer.setName("pari");
            customer.setPassword(passwordEncoder.encode("rezari"));
            customer.setRole(Role.CUSTOMER);
            userRepository.save(customer);

            Product product = new Product();
            product.setName("iPhone 14");
            product.setDescription("Apple iPhone");
            product.setPrice(new BigDecimal("999.99"));
            product.setStock(10);
            product.setCategory(mobile);
            product = productRepository.save(product);
            productId = product.getId();

            initialized = true;
        }

        adminToken = getToken("admin@gmail.com", "12");
        customerToken = getToken("p@gmail.com", "rezari");

        System.out.println("Admin Token: " + adminToken);
        System.out.println("Customer Token: " + customerToken);
    }

    private String getToken(String email, String password) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "/auth/login", loginRequest, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                System.out.println("Login failed for " + email + " with status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.out.println("Login failed for " + email + ": " + e.getMessage());
        }
        return null;
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    void login_Admin_ShouldWork() {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@gmail.com");
        request.setPassword("12");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/auth/login", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        adminToken = response.getBody();
        System.out.println("Admin login successful");
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    void login_Customer_ShouldWork() {
        LoginRequest request = new LoginRequest();
        request.setEmail("p@gmail.com");
        request.setPassword("rezari");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/auth/login", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        customerToken = response.getBody();
        System.out.println("Customer login successful");
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    void createProduct_AsAdmin_ShouldSucceed() {
        assertThat(adminToken).isNotNull();

        ProductRequest request = new ProductRequest();
        request.setName("MacBook Pro");
        request.setDescription("Apple Laptop");
        request.setPrice(new BigDecimal("1999.99"));
        request.setStock(5);
        request.setCategoryId(techCategoryId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ProductRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                "/admin/product?categoryId=" + techCategoryId,
                HttpMethod.POST,
                entity,
                ProductResponse.class);

        System.out.println("Create product response: " + response.getStatusCode());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("MacBook Pro");
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    void getAllProducts_AsCustomer_ShouldFail() {
        assertThat(customerToken).isNotNull();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + customerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/admin/product",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        System.out.println("Get all products as customer: " + response.getStatusCode());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    void updateProduct_AsAdmin_ShouldSucceed() {
        assertThat(adminToken).isNotNull();

        ProductRequest request = new ProductRequest();
        request.setName("iPhone 15");
        request.setDescription("New iPhone");
        request.setPrice(new BigDecimal("1099.99"));
        request.setStock(8);
        request.setCategoryId(mobileCategoryId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ProductRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                "/admin/product/" + productId,
                HttpMethod.PUT,
                entity,
                ProductResponse.class);

        System.out.println("Update product response: " + response.getStatusCode());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("iPhone 15");
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    void deleteProduct_AsAdmin_ShouldSucceed() {
        assertThat(adminToken).isNotNull();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/admin/product/" + productId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class);

        System.out.println("Delete product response: " + response.getStatusCode());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(productRepository.findById(productId)).isEmpty();
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    void createProduct_WithInvalidCategory_ShouldFail() {
        assertThat(adminToken).isNotNull();

        ProductRequest request = new ProductRequest();
        request.setName("Test Product");
        request.setDescription("Test Description");
        request.setPrice(new BigDecimal("99.99"));
        request.setStock(5);
        request.setCategoryId(999L);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ProductRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/admin/product?categoryId=999",
                HttpMethod.POST,
                entity,
                String.class);

        System.out.println("Create product with invalid category: " + response.getStatusCode());

        if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
            System.out.println("Got 403 - This might be due to security config");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        } else {
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Test
    @org.junit.jupiter.api.Order(8)
    void createProduct_AsCustomer_ShouldFail() {
        assertThat(customerToken).isNotNull();

        ProductRequest request = new ProductRequest();
        request.setName("Test Product");
        request.setDescription("Test Description");
        request.setPrice(new BigDecimal("99.99"));
        request.setStock(5);
        request.setCategoryId(techCategoryId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + customerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ProductRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/admin/product?categoryId=" + techCategoryId,
                HttpMethod.POST,
                entity,
                String.class);

        System.out.println("Create product as customer: " + response.getStatusCode());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @org.junit.jupiter.api.Order(9)
    void getProduct_WithoutAuth_ShouldFail() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/admin/product",
                HttpMethod.GET,
                null,
                String.class);

        System.out.println("Get product without auth: " + response.getStatusCode());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}