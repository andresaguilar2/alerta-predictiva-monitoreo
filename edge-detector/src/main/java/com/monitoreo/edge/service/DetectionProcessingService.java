package com.monitoreo.edge.service;

import com.monitoreo.edge.api.DetectionRequest;
import com.monitoreo.edge.config.DetectionRulesProperties;
import com.monitoreo.edge.domain.DetectionEvent;
import com.monitoreo.edge.events.NotifierClient;
import com.monitoreo.edge.rules.CooldownService;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DetectionProcessingService {

    private static final Logger log = LoggerFactory.getLogger(DetectionProcessingService.class);

    private final DetectionRulesProperties rulesProperties;
    private final CooldownService cooldownService;
    private final NotifierClient notifierClient;
    private final MeterRegistry meterRegistry;

    public DetectionProcessingService(
            DetectionRulesProperties rulesProperties,
            CooldownService cooldownService,
            NotifierClient notifierClient,
            MeterRegistry meterRegistry
    ) {
        this.rulesProperties = rulesProperties;
        this.cooldownService = cooldownService;
        this.notifierClient = notifierClient;
        this.meterRegistry = meterRegistry;
    }

    public boolean process(DetectionRequest request) {
        Instant now = Instant.now();
        String normalizedLabel = request.label().toLowerCase(Locale.ROOT);

        if (!"person".equals(normalizedLabel)) {
            log.info("Deteccion ignorada por label no soportada: {}", request.label());
            incrementProcessed("ignored", "label");
            return false;
        }

        if (request.confidence() < rulesProperties.confidenceThreshold()) {
            log.info("Deteccion ignorada por baja confianza: {}", request.confidence());
            incrementProcessed("ignored", "confidence");
            return false;
        }

        if (cooldownService.isInCooldown(request.cameraId(), request.zone(), rulesProperties.cooldownSeconds(), now)) {
            log.info("Deteccion ignorada por cooldown camera={} zone={}", request.cameraId(), request.zone());
            incrementProcessed("ignored", "cooldown");
            return false;
        }

        DetectionEvent event = new DetectionEvent(
                UUID.randomUUID(),
                request.cameraId(),
                now,
                normalizedLabel,
                request.confidence(),
                request.zone(),
                request.imageRef()
        );

        notifierClient.publish(event);
        cooldownService.registerEvent(request.cameraId(), request.zone(), now);
        incrementProcessed("published", "ok");
        return true;
    }

    private void incrementProcessed(String result, String reason) {
        meterRegistry.counter(
                "edge.detections.processed",
                "result", result,
                "reason", reason
        ).increment();
    }
}
