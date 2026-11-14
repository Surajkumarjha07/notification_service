package com.mts.notification.services;

import com.mts.notification.DTOs.RegistrationPayload;
import com.mts.notification.enums.NotificationStatus;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
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
    private final EmailProviderClient emailProviderClient;
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

        deliver(message, messageId);
    }

    public void sendInvitationMail(String messageId, String to, String senderName, String organizationName, String invitationLink) throws Exception {
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

        deliver(message, messageId);
    }

    private void deliver(MimeMessage message, String messageId) {
        try {
            emailProviderClient.send(message);

        } catch (CallNotPermittedException e) {
            log.warn("Email breaker open, returning job to scheduler: {}", messageId);
            reportToScheduler(messageId, "PENDING", false);
            return;
        } catch (MailException e) {
            log.error("Email send failed (retryable) for {}: {}", messageId, e.getMessage());
            String currentJobStatus = reportToScheduler(messageId, "PENDING", true);

            if ("DEAD".equals(currentJobStatus)) {
                notificationService.updateNotification(messageId, NotificationStatus.FAILED);
            }
            return;
        } catch (Exception e) {
            log.error("Email send failed (permanent) for {}: {}", messageId, e.getMessage(), e);
            notificationService.updateNotification(messageId, NotificationStatus.FAILED);
            reportToScheduler(messageId, "DEAD", false);
            return;
        }

        notificationService.updateNotification(messageId, NotificationStatus.DELIVERED);
        reportToScheduler(messageId, "COMPLETED", false);
    }

    private String reportToScheduler(String messageId, String status, boolean countAsAttempt) {
        try {
            return schedulerGrpcReporter.updateJobStatus(messageId, status, countAsAttempt);
        } catch (Exception e) {
            log.warn("Could not report {} for job {} to scheduler: {}", status, messageId, e.getMessage());
            return null;
        }
    }
}
