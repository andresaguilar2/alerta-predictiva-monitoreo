package com.monitoreo.edge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.onnx")
public record OnnxProperties(
        boolean enabled,
        String modelPath,
        int inputWidth,
        int inputHeight,
        int personClassIndex
) {
}
