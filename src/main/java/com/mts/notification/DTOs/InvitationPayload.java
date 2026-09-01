package com.mts.notification.DTOs;

import com.mts.notification.enums.NotificationChannel;
import com.mts.notification.enums.NotificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InvitationPayload(
        @NotBlank @Email String sender,
        @NotBlank String senderName,
        @NotBlank @Email String recipient,
        @NotBlank String organizationName,
        @NotBlank String invitationLink,
        @NotNull NotificationType type,
        @NotNull NotificationChannel channel
) implements NotificationSource {}