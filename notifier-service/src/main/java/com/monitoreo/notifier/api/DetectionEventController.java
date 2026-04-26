package com.monitoreo.notifier.api;

import com.monitoreo.notifier.events.DetectionEvent;
import com.monitoreo.notifier.events.DetectionEventService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events")
public class DetectionEventController {

    private final DetectionEventService detectionEventService;

    public DetectionEventController(DetectionEventService detectionEventService) {
        this.detectionEventService = detectionEventService;
    }

    @PostMapping("/detection")
    public ResponseEntity<String> receiveEvent(@Valid @RequestBody DetectionEvent event) {
        detectionEventService.receive(event);
        return ResponseEntity.accepted().body("Evento recibido y notificado");
    }

    @GetMapping
    public ResponseEntity<List<DetectionEvent>> listEvents() {
        return ResponseEntity.ok(detectionEventService.listAll());
    }
}
