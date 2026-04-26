package com.monitoreo.edge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.detection")
public record DetectionRulesProperties(
        double confidenceThreshold,
        int cooldownSeconds
) {
}
