package com.monitoreo.notifier.notification;

import com.monitoreo.notifier.events.DetectionEvent;

public interface NotificationGateway {
    void send(DetectionEvent event);
}
