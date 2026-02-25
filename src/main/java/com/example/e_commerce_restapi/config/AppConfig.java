package com.example.e_commerce_restapi.config;

import com.example.e_commerce_restapi.dto.request.ProductRequest;
import com.example.e_commerce_restapi.entity.Product;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
