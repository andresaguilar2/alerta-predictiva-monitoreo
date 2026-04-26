package com.monitoreo.notifier.notification;

import com.monitoreo.notifier.events.DetectionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.notifications.log", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LogNotificationGateway implements NotificationGateway {

    private static final Logger log = LoggerFactory.getLogger(LogNotificationGateway.class);

    @Override
    public void send(DetectionEvent event) {
        log.info("Notificacion enviada -> cameraId={} zone={} confidence={}",
                event.cameraId(), event.zone(), event.confidence());
    }
}
