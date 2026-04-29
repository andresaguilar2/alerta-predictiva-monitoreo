package com.monitoreo.notifier.camera;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CameraHealthService {

    private static final Logger log = LoggerFactory.getLogger(CameraHealthService.class);

    private final CameraHealthProperties properties;
    private final CameraHealthAlertService alertService;
    private final Map<String, Instant> lastHeartbeatByCamera = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastAlertByCamera = new ConcurrentHashMap<>();
    private final Map<String, CameraStatus> lastKnownStatusByCamera = new ConcurrentHashMap<>();

    public CameraHealthService(CameraHealthProperties properties, CameraHealthAlertService alertService) {
        this.properties = properties;
        this.alertService = alertService;
    }

    public void receiveStatus(CameraStatusEvent event) {
        Instant now = event.timestamp() != null ? event.timestamp() : Instant.now();
        String cameraId = event.cameraId();
        String reason = normalizeReason(event.reason());

        switch (event.status()) {
            case ONLINE -> {
                lastHeartbeatByCamera.put(cameraId, now);
                lastKnownStatusByCamera.put(cameraId, CameraStatus.ONLINE);
                log.debug("Heartbeat ONLINE recibido cameraId={}", cameraId);
            }
            case ERROR -> {
                lastHeartbeatByCamera.put(cameraId, now);
                lastKnownStatusByCamera.put(cameraId, CameraStatus.ERROR);
                triggerFailureAlertIfAllowed(cameraId, now, reason);
            }
            case OFFLINE -> {
                lastKnownStatusByCamera.put(cameraId, CameraStatus.OFFLINE);
                triggerFailureAlertIfAllowed(cameraId, now, reason);
            }
            default -> log.warn("Estado de camara no soportado cameraId={} status={}", cameraId, event.status());
        }
    }

    @Scheduled(fixedDelayString = "${app.camera-health.monitor-interval-ms:5000}")
    public void monitorHeartbeats() {
        Instant now = Instant.now();
        int timeoutSeconds = Math.max(1, properties.heartbeatTimeoutSeconds());

        for (Map.Entry<String, Instant> entry : lastHeartbeatByCamera.entrySet()) {
            String cameraId = entry.getKey();
            Instant lastHeartbeat = entry.getValue();
            long elapsedSeconds = Duration.between(lastHeartbeat, now).toSeconds();

            if (elapsedSeconds <= timeoutSeconds) {
                continue;
            }

            CameraStatus previousStatus = lastKnownStatusByCamera.get(cameraId);
            if (previousStatus == CameraStatus.OFFLINE) {
                continue;
            }

            lastKnownStatusByCamera.put(cameraId, CameraStatus.OFFLINE);
            triggerFailureAlertIfAllowed(
                    cameraId,
                    now,
                    "No se recibio heartbeat en " + elapsedSeconds + "s (timeout " + timeoutSeconds + "s)"
            );
        }
    }

    private void triggerFailureAlertIfAllowed(String cameraId, Instant now, String reason) {
        Instant lastAlert = lastAlertByCamera.get(cameraId);
        int cooldownSeconds = Math.max(0, properties.alertCooldownSeconds());

        if (lastAlert != null && Duration.between(lastAlert, now).toSeconds() < cooldownSeconds) {
            log.info("Alerta de camara omitida por cooldown cameraId={} reason={}", cameraId, reason);
            return;
        }

        lastAlertByCamera.put(cameraId, now);
        alertService.sendFailureAlert(cameraId, reason, now);
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return "Sin detalle";
        }

        return reason.trim();
    }
}
