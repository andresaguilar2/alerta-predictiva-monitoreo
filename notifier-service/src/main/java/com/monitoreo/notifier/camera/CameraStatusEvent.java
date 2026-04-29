package com.monitoreo.notifier.camera;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CameraStatusEvent(
        @NotBlank String cameraId,
        @NotNull CameraStatus status,
        String reason,
        Instant timestamp
) {
}
