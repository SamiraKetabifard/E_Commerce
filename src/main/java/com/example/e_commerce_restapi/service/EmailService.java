package com.example.e_commerce_restapi.service;

import com.example.e_commerce_restapi.dto.EmailDetails;

public interface EmailService {

    void sendEmail(EmailDetails emailDetails);
}
