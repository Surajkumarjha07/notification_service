package com.mts.notification.services;

import com.mts.notification.DTOs.InvitationPayload;
import com.mts.notification.DTOs.RegistrationPayload;
import com.mts.notification.configs.RabbitQueueConfig;
import jakarta.validation.Valid;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumerService {
    @Autowired
    EmailService emailService;

    @Autowired
    NotificationService notificationService;

    @RabbitListener(queues = RabbitQueueConfig.REGISTRATION_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void consumeRegistrationQueue(@Valid RegistrationPayload message, @Header(AmqpHeaders.MESSAGE_ID) String messageId) throws Exception {
        System.out.println("Message received inside invitation::::::::::: " + message);

        if (notificationService.isMessageAlreadyProcessed(messageId)) {
            System.out.println("Duplicate message ignored:::::::: " + messageId);
            return;
        }

        // save in database to persist notification
        notificationService.saveNotification(message, messageId);

        // to fanout notification to user
        emailService.sendRegistrationMail(message.from(), message.to(), message.username());
    }

    @RabbitListener(queues = RabbitQueueConfig.INVITATION_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void consumeInvitationQueue(@Valid InvitationPayload message, @Header(AmqpHeaders.MESSAGE_ID) String messageId) throws Exception {
        System.out.println("Message received inside invitation::::::::::: " + message);

        // save in database to persist notification
        notificationService.saveNotification(message, messageId);

        // to fanout notification to user
        emailService.sendInvitationMail(
                message.recipient(),
                message.senderName(),
                message.organizationName(),
                message.invitationLink()
        );
    }
}
