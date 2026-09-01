package com.mts.notification.DTOs;

import jakarta.validation.constraints.NotBlank;

public record SimpleMailPayload(
        @NotBlank String to,
        @NotBlank String subject,
        @NotBlank String body
) {}
