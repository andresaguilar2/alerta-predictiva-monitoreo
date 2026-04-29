package com.monitoreo.notifier.events;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

@Component
public class DetectionEventStore {

    private static final Path EVENTS_FILE = Path.of("data", "events.json");

    private final CopyOnWriteArrayList<DetectionEvent> events = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper;

    public DetectionEventStore(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadEventsFromFile() {
        try {
            if (!Files.exists(EVENTS_FILE)) {
                createDataDirectory();
                return;
            }

            List<DetectionEvent> loadedEvents = objectMapper.readValue(
                    EVENTS_FILE.toFile(),
                    new TypeReference<List<DetectionEvent>>() {}
            );

            events.clear();
            events.addAll(loadedEvents);

            System.out.println("Historial cargado desde archivo: " + events.size() + " eventos");

        } catch (IOException e) {
            System.err.println("No se pudo cargar el historial de eventos: " + e.getMessage());
        }
    }

    public void save(DetectionEvent event) {
        events.add(event);
        saveEventsToFile();
    }

    public List<DetectionEvent> findAll() {
        return new ArrayList<>(events);
    }

    private void saveEventsToFile() {
        try {
            createDataDirectory();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(EVENTS_FILE.toFile(), events);
        } catch (IOException e) {
            System.err.println("No se pudo guardar el historial de eventos: " + e.getMessage());
        }
    }

    private void createDataDirectory() throws IOException {
        Files.createDirectories(EVENTS_FILE.getParent());
    }
}