package com.monitoreo.notifier.events;

import com.monitoreo.notifier.notification.NotificationDispatcher;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DetectionEventService {

    private final DetectionEventStore eventStore;
    private final NotificationDispatcher notificationDispatcher;

    public DetectionEventService(DetectionEventStore eventStore, NotificationDispatcher notificationDispatcher) {
        this.eventStore = eventStore;
        this.notificationDispatcher = notificationDispatcher;
    }

    public void receive(DetectionEvent event) {
        DetectionEvent normalized = normalize(event);
        eventStore.save(normalized);
        notificationDispatcher.dispatch(normalized);
    }

    public List<DetectionEvent> listAll() {
        return eventStore.findAll();
    }

    private DetectionEvent normalize(DetectionEvent event) {
        UUID eventId = event.eventId() != null ? event.eventId() : UUID.randomUUID();
        Instant timestamp = event.timestamp() != null ? event.timestamp() : Instant.now();
        return new DetectionEvent(
                eventId,
                event.cameraId(),
                timestamp,
                event.label(),
                event.confidence(),
                event.zone(),
                event.imageRef()
        );
    }
}
