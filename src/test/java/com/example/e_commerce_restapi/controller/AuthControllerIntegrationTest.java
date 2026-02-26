package com.example.e_commerce_restapi.controller;

import com.example.e_commerce_restapi.dto.request.LoginRequest;
import com.example.e_commerce_restapi.dto.request.RegisterRequest;
import com.example.e_commerce_restapi.entity.Role;
import com.example.e_commerce_restapi.entity.User;
import com.example.e_commerce_restapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthControllerIntegrationTest {

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
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User customer = new User();
        customer.setEmail("p@gmail.com");
        customer.setName("pari");
        customer.setPassword(passwordEncoder.encode("rezari"));
        customer.setRole(Role.CUSTOMER);
        userRepository.save(customer);
    }

    @Test
    void register_WithNewEmail_ShouldSucceed() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@gmail.com");
        request.setPassword("password123");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/auth/register", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("User registered successfully");
    }

    @Test
    void register_WithDuplicateEmail_ShouldFail() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("pari");
        request.setEmail("p@gmail.com");
        request.setPassword("rezari");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/auth/register", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("p@gmail.com");
        request.setPassword("rezari");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/auth/login", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length()).isGreaterThan(20);
    }

    @Test
    void login_WithInvalidPassword_ShouldFail() {
        LoginRequest request = new LoginRequest();
        request.setEmail("p@gmail.com");
        request.setPassword("wrongpassword");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/auth/login", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void login_WithNonExistentUser_ShouldFail() {
        LoginRequest request = new LoginRequest();
        request.setEmail("notfound@gmail.com");
        request.setPassword("password");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/auth/login", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}