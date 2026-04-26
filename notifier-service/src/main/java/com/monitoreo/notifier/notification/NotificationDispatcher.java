package com.monitoreo.notifier.notification;

import com.monitoreo.notifier.events.DetectionEvent;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final List<NotificationGateway> gateways;

    public NotificationDispatcher(List<NotificationGateway> gateways) {
        this.gateways = gateways;
    }

    public void dispatch(DetectionEvent event) {
        for (NotificationGateway gateway : gateways) {
            try {
                gateway.send(event);
            } catch (Exception ex) {
                log.error("Fallo envio con gateway {}: {}", gateway.getClass().getSimpleName(), ex.getMessage(), ex);
            }
        }
    }
}
