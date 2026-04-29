package com.monitoreo.notifier.camera;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.camera-health")
public record CameraHealthProperties(
        int heartbeatTimeoutSeconds,
        int alertCooldownSeconds,
        long monitorIntervalMs
) {
}
