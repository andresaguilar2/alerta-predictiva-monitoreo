package com.monitoreo.notifier.prediction;

import com.monitoreo.notifier.events.DetectionEvent;
import com.monitoreo.notifier.events.DetectionEventStore;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PredictionService {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Bogota");
    private static final int WINDOW_HOURS = 2;

    private final DetectionEventStore eventStore;

    public PredictionService(DetectionEventStore eventStore) {
        this.eventStore = eventStore;
    }

    public PredictiveAlert getCurrentPrediction() {
        List<DetectionEvent> events = eventStore.findAll();

        if (events.isEmpty()) {
            return new PredictiveAlert(
                    Instant.now(),
                    0,
                    "Sin datos",
                    0,
                    0.0,
                    "Sin datos",
                    "Aún no hay eventos registrados. El sistema empezará a generar predicciones cuando detecte personas."
            );
        }

        Map<Integer, Long> eventsByTimeWindow = events.stream()
                .filter(event -> event.timestamp() != null)
                .collect(Collectors.groupingBy(
                        event -> getTimeWindow(event.timestamp()),
                        Collectors.counting()
                ));

        Map.Entry<Integer, Long> mostActiveWindow = eventsByTimeWindow.entrySet()
                .stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .orElseThrow();

        int windowIndex = mostActiveWindow.getKey();
        int eventCount = mostActiveWindow.getValue().intValue();
        int totalEvents = events.size();

        double probabilityPercent = Math.round((eventCount * 1000.0) / totalEvents) / 10.0;

        int startHour = windowIndex * WINDOW_HOURS;
        int endHour = startHour + WINDOW_HOURS;

        String criticalWindow = formatWindow(startHour, endHour);
        String riskLevel = calculateRiskLevel(totalEvents, eventCount, probabilityPercent);
        String recommendation = buildRecommendation(riskLevel, criticalWindow, eventCount, probabilityPercent);

        return new PredictiveAlert(
                Instant.now(),
                totalEvents,
                criticalWindow,
                eventCount,
                probabilityPercent,
                riskLevel,
                recommendation
        );
    }

    private int getTimeWindow(Instant timestamp) {
        ZonedDateTime dateTime = timestamp.atZone(ZONE_ID);
        int hour = dateTime.getHour();
        return hour / WINDOW_HOURS;
    }

    private String formatWindow(int startHour, int endHour) {
        String start = String.format("%02d:00", startHour);
        String end = endHour >= 24 ? "00:00" : String.format("%02d:00", endHour);
        return start + " - " + end;
    }

    private String calculateRiskLevel(int totalEvents, int eventCount, double probabilityPercent) {
        if (totalEvents < 3) {
            return "Datos iniciales";
        }

        if (eventCount >= 7 || (totalEvents >= 10 && probabilityPercent >= 50)) {
            return "Alto";
        }

        if (eventCount >= 3 || (totalEvents >= 5 && probabilityPercent >= 30)) {
            return "Medio";
        }

        return "Bajo";
    }

    private String buildRecommendation(String riskLevel, String criticalWindow, int eventCount, double probabilityPercent) {
        if ("Sin datos".equals(riskLevel)) {
            return "Aún no hay datos suficientes para generar una alerta predictiva.";
        }

        if ("Datos iniciales".equals(riskLevel)) {
            return "El sistema está recopilando eventos. Por ahora la predicción es preliminar.";
        }

        return "El horario con mayor actividad detectada es " + criticalWindow
                + ". Se han registrado " + eventCount
                + " eventos en esta franja, equivalente al " + probabilityPercent
                + "% del historial. Se recomienda estar atento en este horario.";
    }
}