package com.monitoreo.edge.events;

import com.monitoreo.edge.config.NotifierProperties;
import com.monitoreo.edge.domain.DetectionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpNotifierClient implements NotifierClient {

    private static final Logger log = LoggerFactory.getLogger(HttpNotifierClient.class);

    private final RestClient restClient;

    public HttpNotifierClient(NotifierProperties notifierProperties) {
        this.restClient = RestClient.builder()
                .baseUrl(notifierProperties.baseUrl())
                .build();
    }

    @Override
    public void publish(DetectionEvent event) {
        restClient.post()
                .uri("/api/v1/events/detection")
                .contentType(MediaType.APPLICATION_JSON)
                .body(event)
                .retrieve()
                .toBodilessEntity();

        log.info("Evento enviado a notifier-service: cameraId={} zone={} confidence={}",
                event.cameraId(), event.zone(), event.confidence());
    }
}
