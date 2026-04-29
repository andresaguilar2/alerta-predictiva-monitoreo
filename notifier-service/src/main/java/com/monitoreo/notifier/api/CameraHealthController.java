package com.monitoreo.notifier.api;

import com.monitoreo.notifier.camera.CameraHealthService;
import com.monitoreo.notifier.camera.CameraStatusEvent;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cameras")
public class CameraHealthController {

    private final CameraHealthService cameraHealthService;

    public CameraHealthController(CameraHealthService cameraHealthService) {
        this.cameraHealthService = cameraHealthService;
    }

    @PostMapping("/status")
    public ResponseEntity<String> receiveStatus(@Valid @RequestBody CameraStatusEvent event) {
        cameraHealthService.receiveStatus(event);
        return ResponseEntity.accepted().body("Estado de camara recibido");
    }
}
