package com.example.e_commerce_restapi.service;
import com.example.e_commerce_restapi.dto.EmailDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Override
    public void sendEmail(EmailDetails details) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(details.getRecipient());
            message.setSubject(details.getSubject());
            message.setText(details.getMessageBody());

            mailSender.send(message);
            log.info("Email sent successfully to: {}", details.getRecipient());
        } catch (Exception e) {
            log.error("Failed to send email to: {}", details.getRecipient(), e);
        }
    }
}
