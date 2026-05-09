package com.monitoreo.notifier.notification;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.notifications.email", name = "enabled", havingValue = "true")
public class PendingEmailStore {

    private static final Logger log = LoggerFactory.getLogger(PendingEmailStore.class);

    private static final Path PENDING_EMAILS_FILE = Path.of("data", "pending-emails.json");

    private final CopyOnWriteArrayList<PendingEmailNotification> pendingEmails = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper;

    public PendingEmailStore(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadPendingEmails() {
        try {
            createDataDirectory();

            if (!Files.exists(PENDING_EMAILS_FILE)) {
                saveToFile();
                return;
            }

            List<PendingEmailNotification> loaded =
                    objectMapper.readValue(PENDING_EMAILS_FILE.toFile(), new TypeReference<>() {});

            pendingEmails.clear();
            pendingEmails.addAll(loaded);

            log.info("Correos pendientes cargados: {}", pendingEmails.size());

        } catch (IOException e) {
            log.warn("No se pudieron cargar los correos pendientes: {}", e.getMessage());
        }
    }

    public synchronized void add(PendingEmailNotification pendingEmail) {
        pendingEmails.add(pendingEmail);
        saveToFile();
    }

    public List<PendingEmailNotification> findAll() {
        return new ArrayList<>(pendingEmails);
    }

    public synchronized void remove(UUID id) {
        pendingEmails.removeIf(email -> email.id().equals(id));
        saveToFile();
    }

    public synchronized void update(PendingEmailNotification updatedEmail) {
        pendingEmails.removeIf(email -> email.id().equals(updatedEmail.id()));
        pendingEmails.add(updatedEmail);
        saveToFile();
    }

    private void saveToFile() {
        try {
            createDataDirectory();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(PENDING_EMAILS_FILE.toFile(), pendingEmails);
        } catch (IOException e) {
            log.warn("No se pudieron guardar los correos pendientes: {}", e.getMessage());
        }
    }

    private void createDataDirectory() throws IOException {
        Files.createDirectories(PENDING_EMAILS_FILE.getParent());
    }
}
