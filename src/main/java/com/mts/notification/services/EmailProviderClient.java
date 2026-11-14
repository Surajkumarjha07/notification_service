package com.mts.notification.services;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailProviderClient {
    private final JavaMailSender mailSender;

    @CircuitBreaker(name = "emailProvider")
    public void send(MimeMessage message) {
        mailSender.send(message);
    }
}
