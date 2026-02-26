package com.example.e_commerce_restapi.controller;

import com.example.e_commerce_restapi.dto.request.LoginRequest;
import com.example.e_commerce_restapi.entity.Category;
import com.example.e_commerce_restapi.entity.Role;
import com.example.e_commerce_restapi.entity.User;
import com.example.e_commerce_restapi.repository.CategoryRepository;
import com.example.e_commerce_restapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CategoryControllerIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:latest")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String customerToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        categoryRepository.deleteAll();

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

        Category tech = new Category();
        tech.setName("tech");
        categoryRepository.save(tech);

        Category mobile = new Category();
        mobile.setName("mobile");
        categoryRepository.save(mobile);

        adminToken = getToken("admin@gmail.com", "12");
        customerToken = getToken("p@gmail.com", "rezari");
    }

    private String getToken(String email, String password) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/auth/login", loginRequest, String.class);
        return response.getBody();
    }

    @Test
    void createCategory_AsAdmin_ShouldSucceed() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);

        ResponseEntity<Category> response = restTemplate.exchange(
                "/admin/category?name=Electronics",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                Category.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Electronics");
    }

    @Test
    void createCategory_AsCustomer_ShouldFail() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + customerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/admin/category?name=Electronics",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getAllCategories_ShouldReturnList() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);

        ResponseEntity<List> response = restTemplate.exchange(
                "/admin/category",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isEqualTo(2);
    }
}