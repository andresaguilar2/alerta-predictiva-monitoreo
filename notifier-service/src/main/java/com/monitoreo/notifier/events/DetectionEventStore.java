package com.monitoreo.notifier.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

@Component
public class DetectionEventStore {

    private final CopyOnWriteArrayList<DetectionEvent> events = new CopyOnWriteArrayList<>();

    public void save(DetectionEvent event) {
        events.add(event);
    }

    public List<DetectionEvent> findAll() {
        return new ArrayList<>(events);
    }
}
