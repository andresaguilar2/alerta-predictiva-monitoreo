package com.monitoreo.notifier.prediction;

import java.time.Instant;

public record PredictiveAlert(
        Instant generatedAt,
        int totalEvents,
        String criticalWindow,
        int eventCountInWindow,
        double probabilityPercent,
        String riskLevel,
        String recommendation
) {
}