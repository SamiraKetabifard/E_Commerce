package com.example.e_commerce_restapi.controller;

import com.example.e_commerce_restapi.dto.request.LoginRequest;
import com.example.e_commerce_restapi.dto.request.RegisterRequest;
import com.example.e_commerce_restapi.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;

    }

    @PostMapping(path = "/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequest request){

        return  ResponseEntity.ok(authService.register(request));
    }

    @PostMapping(path="/login")
    public ResponseEntity<String> login(@RequestBody @Valid LoginRequest request){

        return ResponseEntity.ok(authService.login(request));
    }
}
