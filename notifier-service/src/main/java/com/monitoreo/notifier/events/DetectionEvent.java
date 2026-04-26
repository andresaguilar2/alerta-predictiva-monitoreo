package com.monitoreo.notifier.events;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

public record DetectionEvent(
        UUID eventId,
        @NotBlank String cameraId,
        Instant timestamp,
        @NotBlank String label,
        @Min(0) @Max(1) double confidence,
        @NotBlank String zone,
        String imageRef
) {
}
