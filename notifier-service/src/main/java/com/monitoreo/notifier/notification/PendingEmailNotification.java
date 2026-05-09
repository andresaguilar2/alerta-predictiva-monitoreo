package com.monitoreo.notifier.notification;

import java.time.Instant;
import java.util.UUID;

public record PendingEmailNotification(
        UUID id,
        String from,
        String to,
        String subject,
        String body,
        Instant createdAt,
        int attempts,
        String lastError
) {}
