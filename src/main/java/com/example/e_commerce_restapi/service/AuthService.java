package com.example.e_commerce_restapi.service;

import com.example.e_commerce_restapi.config.JwtService;
import com.example.e_commerce_restapi.dto.request.LoginRequest;
import com.example.e_commerce_restapi.dto.request.RegisterRequest;
import com.example.e_commerce_restapi.entity.Role;
import com.example.e_commerce_restapi.entity.User;
import com.example.e_commerce_restapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public String register(RegisterRequest request){

        if (userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new RuntimeException("Email already exists");
        }
        User user= new User();
        user.setEmail(request.getEmail());
        user.setName(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);

        return "User registered successfully";
    }
    public String login(LoginRequest request){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                        request.getEmail(),request.getPassword()));

        UserDetails userDetails= new
                org.springframework.security.core.userdetails.User(
                request.getEmail(), "", List.of());

        return jwtService.generateToken(userDetails);
    }
}

