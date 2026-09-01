package com.mts.notification.services;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    @Autowired
    JavaMailSender mailSender;

    @Autowired
    TemplateEngine templateEngine;

    public void sendSimpleMail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    public void sendRegistrationMail(String from, String to, String username) throws Exception {
        Context context = new Context();
        context.setVariable("from", from);
        context.setVariable("username", username);
        context.setVariable("email", to);

        String htmlBody = templateEngine.process("welcome-mail", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Welcome to Our App!");
        helper.setText(htmlBody, true);

        mailSender.send(message);
    }

    public void sendInvitationMail(String to, String senderName, String organizationName, String invitationLink) throws Exception {
        Context context = new Context();
        context.setVariable("senderName", senderName);
        context.setVariable("organizationName", organizationName);
        context.setVariable("invitationLink", invitationLink);

        String htmlBody = templateEngine.process("invitation-mail", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(senderName + " invited you to join " + organizationName);
        helper.setText(htmlBody, true);

        mailSender.send(message);
    }
}
