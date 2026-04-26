package com.monitoreo.edge.domain;

import java.time.Instant;
import java.util.UUID;

public record DetectionEvent(
        UUID eventId,
        String cameraId,
        Instant timestamp,
        String label,
        double confidence,
        String zone,
        String imageRef
) {
}
