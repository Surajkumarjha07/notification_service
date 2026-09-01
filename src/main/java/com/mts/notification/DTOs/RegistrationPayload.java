package com.mts.notification.DTOs;

import com.mts.notification.enums.NotificationChannel;
import com.mts.notification.enums.NotificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegistrationPayload(
        @NotBlank String from,
        @Email String to,
        @NotBlank String username,
        @NotNull NotificationType type,
        @NotNull NotificationChannel channel
) implements NotificationSource {
    @Override public String sender() { return from; }
    @Override public String recipient() { return to; }
}
