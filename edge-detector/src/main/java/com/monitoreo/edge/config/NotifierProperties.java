package com.monitoreo.edge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.notifier")
public record NotifierProperties(String baseUrl) {
}
