package com.monitoreo.edge.api;

public record OnnxImageDetectionResponse(
        boolean onnxEnabled,
        boolean personDetected,
        double modelConfidence,
        boolean published,
        String message
) {
}
