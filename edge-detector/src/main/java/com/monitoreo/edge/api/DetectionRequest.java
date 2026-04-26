package com.monitoreo.edge.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DetectionRequest(
        @NotBlank String cameraId,
        @NotBlank @Size(max = 50) String label,
        @Min(0) @Max(1) double confidence,
        @NotBlank String zone,
        @Size(max = 512) String imageRef
) {
}
