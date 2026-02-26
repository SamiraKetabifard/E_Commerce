package com.example.e_commerce_restapi.controller;

import com.example.e_commerce_restapi.dto.request.LoginRequest;
import com.example.e_commerce_restapi.entity.*;
import com.example.e_commerce_restapi.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
public class CartControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String customerToken;
    private Long productId;
    private User customer;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        Category mobile = new Category();
        mobile.setName("mobile");
        mobile = categoryRepository.save(mobile);

        Product product = new Product();
        product.setName("iPhone 14");
        product.setDescription("Apple iPhone");
        product.setPrice(new BigDecimal("999.99"));
        product.setStock(10);
        product.setCategory(mobile);
        productId = productRepository.save(product).getId();

        customer = new User();
        customer.setEmail("p@gmail.com");
        customer.setName("pari");
        customer.setPassword(passwordEncoder.encode("rezari"));
        customer.setRole(Role.CUSTOMER);
        customer = userRepository.save(customer);

        Cart cart = new Cart();
        cart.setUser(customer);
        cart.setItems(new java.util.ArrayList<>());
        cartRepository.save(cart);

        System.out.println("=== Testing Login ===");
        testLogin();

        customerToken = getToken("p@gmail.com", "rezari");
        System.out.println("Token from getToken(): " + customerToken);

        assertThat(customerToken)
                .withFailMessage("Token is null - login failed! Check if /auth/login endpoint works")
                .isNotNull();
    }

    private void testLogin() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("p@gmail.com");
        loginRequest.setPassword("rezari");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/auth/login", loginRequest, String.class);

        System.out.println("Login Status: " + response.getStatusCode());
        System.out.println("Login Body: " + response.getBody());
        System.out.println("Login Headers: " + response.getHeaders());
    }

    private String getToken(String email, String password) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/auth/login", loginRequest, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            System.out.println("Login failed with status: " + response.getStatusCode());
            System.out.println("Response body: " + response.getBody());
            return null;
        }
    }

    @Test
    void addToCart_AsCustomer_ShouldSucceed() {
        assertThat(customerToken).isNotNull();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + customerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Cart> response = restTemplate.exchange(
                "/cart/add/" + productId + "?quantity=2",
                HttpMethod.POST,
                entity,
                Cart.class);

        System.out.println("Add to cart status: " + response.getStatusCode());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void login_ShouldWork() {
        LoginRequest request = new LoginRequest();
        request.setEmail("p@gmail.com");
        request.setPassword("rezari");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/auth/login", request, String.class);

        System.out.println("Login test - Status: " + response.getStatusCode());
        System.out.println("Login test - Body: " + response.getBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}