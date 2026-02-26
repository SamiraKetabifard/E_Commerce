package com.example.e_commerce_restapi.service;

import com.example.e_commerce_restapi.dto.request.LoginRequest;
import com.example.e_commerce_restapi.dto.request.RegisterRequest;
import com.example.e_commerce_restapi.entity.Role;
import com.example.e_commerce_restapi.entity.User;
import com.example.e_commerce_restapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
public class AuthServiceUnitTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("pari");
        registerRequest.setEmail("p@gmail.com");
        registerRequest.setPassword("rezari");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("p@gmail.com");
        loginRequest.setPassword("rezari");
    }

    @Test
    void register_WithNewEmail_ShouldSaveUserAndReturnSuccess() {
        String result = authService.register(registerRequest);
        assertThat(result).isEqualTo("User registered successfully");
        User savedUser = userRepository.findByEmail("p@gmail.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("pari");
        assertThat(savedUser.getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void register_WithDuplicateEmail_ShouldThrowException() {
        authService.register(registerRequest);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already exists");
    }
    @Test
    void login_WithValidCredentials_ShouldReturnToken() {
        authService.register(registerRequest);

        String token = authService.login(loginRequest);

        assertThat(token).isNotNull();
        assertThat(token.length()).isGreaterThan(20);
    }
    @Test
    void login_WithInvalidPassword_ShouldThrowException() {
        authService.register(registerRequest);

        LoginRequest wrongRequest = new LoginRequest();
        wrongRequest.setEmail("p@gmail.com");
        wrongRequest.setPassword("wrongpassword");

        assertThatThrownBy(() -> authService.login(wrongRequest))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void login_WithNonExistentUser_ShouldThrowException() {
        LoginRequest nonExistentRequest = new LoginRequest();
        nonExistentRequest.setEmail("notfound@gmail.com");
        nonExistentRequest.setPassword("password");

        assertThatThrownBy(() -> authService.login(nonExistentRequest))
                .isInstanceOf(RuntimeException.class);
    }
}