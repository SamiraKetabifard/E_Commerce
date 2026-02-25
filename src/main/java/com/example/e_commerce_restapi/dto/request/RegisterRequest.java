package com.example.e_commerce_restapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "username is required")
    @NotNull
    private String username;

    @NotBlank(message = "Email is required")
    @NotNull
    private String email;

    @NotBlank(message ="password is required")
    @NotNull
    private String password;
}
