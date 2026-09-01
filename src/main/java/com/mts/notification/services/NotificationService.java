package com.mts.notification.services;

import com.mts.notification.DTOs.NotificationSource;
import com.mts.notification.entities.NotificationEntity;
import com.mts.notification.enums.NotificationStatus;
import com.mts.notification.repositories.NotificationRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    NotificationRepository notificationRepository;

    public void saveNotification(@Valid NotificationSource request, String messageId) {
        NotificationEntity notification = new NotificationEntity();

        notification.setNotificationId(messageId);
        notification.setSender(request.sender());
        notification.setRecipientId(request.recipient());
        notification.setType(request.type());
        notification.setChannel(request.channel());
        notification.setStatus(NotificationStatus.PENDING);

        notificationRepository.save(notification);
    }

    public boolean isMessageAlreadyProcessed(String messageId) {
        Optional<NotificationEntity> fetchedMessage = notificationRepository.findById(messageId);

        return fetchedMessage.isPresent();
    }

    public void updateNotification() {

    }
}
