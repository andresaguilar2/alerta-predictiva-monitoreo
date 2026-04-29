package com.monitoreo.notifier.notification;

import com.monitoreo.notifier.events.DetectionEvent;
import java.time.Instant;
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
        String subject = buildDetectionSubject(notificationProperties.email().subjectPrefix());
        String body = buildDetectionBody(event);
        sendEmail(subject, body);
        log.info("Email enviado a {} para evento {}", notificationProperties.email().to(), event.eventId());
    }

    public void sendCameraFailureAlert(String cameraId, String reason, Instant timestamp) {
        String subject = buildCameraFailureSubject(notificationProperties.email().subjectPrefix());
        String body = buildCameraFailureBody(cameraId, reason, timestamp);
        sendEmail(subject, body);
        log.info("Email de falla de camara enviado a {} para cameraId={}", notificationProperties.email().to(), cameraId);
    }

    private String buildDetectionSubject(String subjectPrefix) {
        String prefix = isBlank(subjectPrefix) ? "[Monitoreo Casas]" : subjectPrefix.trim();
        return prefix + " Alerta de persona detectada";
    }

    private String buildDetectionBody(DetectionEvent event) {
        return "Se detecto una persona.\n"
                + "cameraId: " + event.cameraId() + "\n"
                + "zona: " + event.zone() + "\n"
                + "confidence: " + event.confidence() + "\n"
                + "timestamp: " + event.timestamp() + "\n"
                + "eventId: " + event.eventId() + "\n";
    }

    private String buildCameraFailureSubject(String subjectPrefix) {
        String prefix = isBlank(subjectPrefix) ? "[Monitoreo Casas]" : subjectPrefix.trim();
        return prefix + " Falla de camara detectada";
    }

    private String buildCameraFailureBody(String cameraId, String reason, Instant timestamp) {
        return "Se detecto una falla en la camara.\n"
                + "cameraId: " + cameraId + "\n"
                + "motivo: " + reason + "\n"
                + "timestamp: " + timestamp + "\n";
    }

    private void sendEmail(String subject, String body) {
        NotificationProperties.Email emailCfg = notificationProperties.email();
        if (isBlank(emailCfg.from()) || isBlank(emailCfg.to())) {
            log.warn("Email habilitado pero falta from/to. Se omite envio.");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailCfg.from());
        message.setTo(emailCfg.to());
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
