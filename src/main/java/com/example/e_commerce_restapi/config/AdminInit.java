package com.example.e_commerce_restapi.config;

import com.example.e_commerce_restapi.entity.Role;
import com.example.e_commerce_restapi.entity.User;
import com.example.e_commerce_restapi.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInit implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public AdminInit(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    public void run(String... args) throws Exception {
        if(userRepository.findByEmail("admin@gmail.com").isEmpty()){
            User admin=new User();
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("12"));
            admin.setRole(Role.ADMIN);
            admin.setName("admin");
            userRepository.save(admin);
        }
    }

}
