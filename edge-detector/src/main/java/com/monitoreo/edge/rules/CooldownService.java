package com.monitoreo.edge.rules;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class CooldownService {

    private final ConcurrentHashMap<String, Instant> lastEventByKey = new ConcurrentHashMap<>();

    public boolean isInCooldown(String cameraId, String zone, int cooldownSeconds, Instant now) {
        String key = cameraId + "::" + zone;
        Instant lastEvent = lastEventByKey.get(key);

        if (lastEvent == null) {
            return false;
        }

        long elapsedSeconds = Duration.between(lastEvent, now).toSeconds();
        return elapsedSeconds < cooldownSeconds;
    }

    public void registerEvent(String cameraId, String zone, Instant when) {
        String key = cameraId + "::" + zone;
        lastEventByKey.put(key, when);
    }
}
