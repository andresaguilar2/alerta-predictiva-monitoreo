package com.monitoreo.notifier.notification;

import com.monitoreo.notifier.events.DetectionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.notifications.email", name = "enabled", havingValue = "true")
public class EmailNotificationGateway implements NotificationGateway {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationGateway.class);

    private final JavaMailSender mailSender;
    private final NotificationProperties notificationProperties;

    public EmailNotificationGateway(JavaMailSender mailSender, NotificationProperties notificationProperties) {
        this.mailSender = mailSender;
        this.notificationProperties = notificationProperties;
    }

    @Override
    public void send(DetectionEvent event) {
        NotificationProperties.Email emailCfg = notificationProperties.email();
        if (isBlank(emailCfg.from()) || isBlank(emailCfg.to())) {
            log.warn("Email habilitado pero falta from/to. Se omite envio.");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailCfg.from());
        message.setTo(emailCfg.to());
        message.setSubject(buildSubject(emailCfg.subjectPrefix()));
        message.setText(buildBody(event));
        mailSender.send(message);
        log.info("Email enviado a {} para evento {}", emailCfg.to(), event.eventId());
    }

    private String buildSubject(String subjectPrefix) {
        String prefix = isBlank(subjectPrefix) ? "[Monitoreo Casas]" : subjectPrefix.trim();
        return prefix + " Alerta de persona detectada";
    }

    private String buildBody(DetectionEvent event) {
        return "Se detecto una persona.\n"
                + "cameraId: " + event.cameraId() + "\n"
                + "zona: " + event.zone() + "\n"
                + "confidence: " + event.confidence() + "\n"
                + "timestamp: " + event.timestamp() + "\n"
                + "eventId: " + event.eventId() + "\n";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
