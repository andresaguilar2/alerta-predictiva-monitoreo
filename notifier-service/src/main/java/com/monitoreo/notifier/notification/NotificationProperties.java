package com.monitoreo.notifier.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.notifications")
public record NotificationProperties(
        Email email,
        Whatsapp whatsapp
) {
    public record Email(
            boolean enabled,
            String from,
            String to,
            String subjectPrefix
    ) {
    }

    public record Whatsapp(
            boolean enabled,
            String apiUrl,
            String accessToken,
            String phoneNumberId,
            String to
    ) {
    }
}
