package com.monitoreo.notifier.notification;

import com.monitoreo.notifier.events.DetectionEvent;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(prefix = "app.notifications.whatsapp", name = "enabled", havingValue = "true")
public class WhatsappNotificationGateway implements NotificationGateway {

    private static final Logger log = LoggerFactory.getLogger(WhatsappNotificationGateway.class);

    private final NotificationProperties notificationProperties;

    public WhatsappNotificationGateway(NotificationProperties notificationProperties) {
        this.notificationProperties = notificationProperties;
    }

    @Override
    public void send(DetectionEvent event) {
        NotificationProperties.Whatsapp cfg = notificationProperties.whatsapp();
        if (isBlank(cfg.apiUrl()) || isBlank(cfg.phoneNumberId()) || isBlank(cfg.accessToken()) || isBlank(cfg.to())) {
            log.warn("WhatsApp habilitado pero falta apiUrl/phoneNumberId/accessToken/to. Se omite envio.");
            return;
        }

        String endpoint = cfg.apiUrl().replaceAll("/$", "") + "/" + cfg.phoneNumberId() + "/messages";
        String bodyText = "Alerta Monitoreo Casas: persona detectada en "
                + event.zone()
                + " (camara "
                + event.cameraId()
                + ", confianza "
                + event.confidence()
                + ").";

        RestClient client = RestClient.builder().build();
        client.post()
                .uri(endpoint)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + cfg.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "messaging_product", "whatsapp",
                        "to", cfg.to(),
                        "type", "text",
                        "text", Map.of(
                                "preview_url", false,
                                "body", bodyText
                        )
                ))
                .retrieve()
                .toBodilessEntity();
        log.info("WhatsApp enviado a {} para evento {}", cfg.to(), event.eventId());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
