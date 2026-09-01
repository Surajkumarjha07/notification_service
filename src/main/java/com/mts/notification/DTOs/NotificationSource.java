package com.mts.notification.DTOs;

import com.mts.notification.enums.NotificationChannel;
import com.mts.notification.enums.NotificationType;

public interface NotificationSource {
    String sender();
    String recipient();
    NotificationType type();
    NotificationChannel channel();
}