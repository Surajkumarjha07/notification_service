package com.mts.notification.services;

import com.mts.notification.DTOs.RegistrationPayload;
import com.mts.notification.enums.NotificationStatus;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final NotificationService notificationService;
    private final SchedulerGrpcReporter schedulerGrpcReporter;

    public void sendSimpleMail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    public void sendRegistrationMail(RegistrationPayload payload, String messageId) throws Exception {
        try {
            Context context = new Context();
            context.setVariable("from", payload.from());
            context.setVariable("username", payload.username());
            context.setVariable("email", payload.to());

            String htmlBody = templateEngine.process("welcome-mail", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(payload.to());
            helper.setSubject("Welcome to Our App!");
            helper.setText(htmlBody, true);

            mailSender.send(message);
            notificationService.updateNotification(messageId, NotificationStatus.DELIVERED);
            schedulerGrpcReporter.updateJobStatus(messageId, "COMPLETED");
        } catch (Exception e) {
            log.error("Error in sending registration email ===== {}", e.getMessage());
            notificationService.updateNotification(messageId, NotificationStatus.FAILED);
        }
    }

    public void sendInvitationMail(String messageId, String to, String senderName, String organizationName, String invitationLink) throws Exception {
        try {
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
            notificationService.updateNotification(messageId, NotificationStatus.DELIVERED);
            schedulerGrpcReporter.updateJobStatus(messageId, "COMPLETED");
        } catch (Exception e) {
            log.error("Error in sending invitation email ===== {}", e.getMessage());
            notificationService.updateNotification(messageId, NotificationStatus.FAILED);
        }
    }
}
