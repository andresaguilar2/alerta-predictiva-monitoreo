package com.monitoreo.notifier.notification;

import com.monitoreo.notifier.events.DetectionEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.notifications.email", name = "enabled", havingValue = "true")
public class EmailNotificationGateway implements NotificationGateway {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationGateway.class);

    private final JavaMailSender mailSender;
    private final NotificationProperties notificationProperties;
    private final PendingEmailStore pendingEmailStore;

    public EmailNotificationGateway(
            JavaMailSender mailSender,
            NotificationProperties notificationProperties,
            PendingEmailStore pendingEmailStore
    ) {
        this.mailSender = mailSender;
        this.notificationProperties = notificationProperties;
        this.pendingEmailStore = pendingEmailStore;
    }

    @Override
    public void send(DetectionEvent event) {
        NotificationProperties.Email emailCfg = notificationProperties.email();

        if (isBlank(emailCfg.from()) || isBlank(emailCfg.to())) {
            log.warn("Email habilitado pero falta from/to. Se omite envio.");
            return;
        }

        PendingEmailNotification pendingEmail = new PendingEmailNotification(
                UUID.randomUUID(),
                emailCfg.from(),
                emailCfg.to(),
                buildSubject(emailCfg.subjectPrefix()),
                buildBody(event),
                Instant.now(),
                0,
                null
        );

        try {
            sendPendingEmail(pendingEmail);
            log.info("Email enviado a {} para evento {}", emailCfg.to(), event.eventId());
        } catch (Exception ex) {
            PendingEmailNotification failedEmail = new PendingEmailNotification(
                    pendingEmail.id(),
                    pendingEmail.from(),
                    pendingEmail.to(),
                    pendingEmail.subject(),
                    pendingEmail.body(),
                    pendingEmail.createdAt(),
                    pendingEmail.attempts() + 1,
                    ex.getMessage()
            );

            pendingEmailStore.add(failedEmail);

            log.warn(
                    "No se pudo enviar el correo del evento {}. Se guardo en cola de pendientes. Error: {}",
                    event.eventId(),
                    ex.getMessage()
            );
        }
    }

    @Scheduled(fixedDelay = 10000)
    public void retryPendingEmails() {
        List<PendingEmailNotification> pendingEmails = pendingEmailStore.findAll();

        if (pendingEmails.isEmpty()) {
            return;
        }

        log.info("Reintentando envio de correos pendientes. Total: {}", pendingEmails.size());

        for (PendingEmailNotification pendingEmail : pendingEmails) {
            try {
                sendPendingEmail(pendingEmail);
                pendingEmailStore.remove(pendingEmail.id());

                log.info("Correo pendiente enviado correctamente a {}", pendingEmail.to());

            } catch (Exception ex) {
                PendingEmailNotification updatedEmail = new PendingEmailNotification(
                        pendingEmail.id(),
                        pendingEmail.from(),
                        pendingEmail.to(),
                        pendingEmail.subject(),
                        pendingEmail.body(),
                        pendingEmail.createdAt(),
                        pendingEmail.attempts() + 1,
                        ex.getMessage()
                );

                pendingEmailStore.update(updatedEmail);

                log.warn(
                        "Aun no se pudo enviar correo pendiente {}. Intentos: {}. Error: {}",
                        pendingEmail.id(),
                        updatedEmail.attempts(),
                        ex.getMessage()
                );
            }
        }
    }

    private void sendPendingEmail(PendingEmailNotification pendingEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(pendingEmail.from());
        message.setTo(pendingEmail.to());
        message.setSubject(pendingEmail.subject());
        message.setText(pendingEmail.body());

        mailSender.send(message);
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