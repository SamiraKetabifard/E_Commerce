package com.example.e_commerce_restapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailDetails {
    private String recipient;
    private String subject;
    private String messageBody;
}
