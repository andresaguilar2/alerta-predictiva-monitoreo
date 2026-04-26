package com.monitoreo.edge.api;

import com.monitoreo.edge.inference.OnnxPersonDetectionService;
import com.monitoreo.edge.inference.PersonDetectionResult;
import com.monitoreo.edge.service.DetectionProcessingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/detections")
@CrossOrigin(origins = "*")
public class DetectionSimulationController {

    private final DetectionProcessingService detectionProcessingService;
    private final OnnxPersonDetectionService onnxPersonDetectionService;

    public DetectionSimulationController(
            DetectionProcessingService detectionProcessingService,
            OnnxPersonDetectionService onnxPersonDetectionService
    ) {
        this.detectionProcessingService = detectionProcessingService;
        this.onnxPersonDetectionService = onnxPersonDetectionService;
    }

    @PostMapping("/test")
    public ResponseEntity<String> testDetection(@Valid @RequestBody DetectionRequest request) {
        boolean published = detectionProcessingService.process(request);

        if (!published) {
            return ResponseEntity.accepted().body("Deteccion recibida pero no publicada por reglas");
        }

        return ResponseEntity.ok("Deteccion publicada correctamente");
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OnnxImageDetectionResponse> detectFromImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam(defaultValue = "cam-01") String cameraId,
            @RequestParam(defaultValue = "entrada") String zone
    ) {
        if (!onnxPersonDetectionService.isEnabled()) {
            return ResponseEntity.badRequest().body(new OnnxImageDetectionResponse(
                    false,
                    false,
                    0,
                    false,
                    "ONNX deshabilitado. Configura app.onnx.enabled=true y un modelo valido"
            ));
        }

        PersonDetectionResult result = onnxPersonDetectionService.detectPerson(image);
        if (!result.personDetected()) {
            return ResponseEntity.accepted().body(new OnnxImageDetectionResponse(
                    true,
                    false,
                    0,
                    false,
                    "No se detecto persona en la imagen"
            ));
        }

        DetectionRequest request = new DetectionRequest(
                cameraId,
                "person",
                result.confidence(),
                zone,
                image.getOriginalFilename() == null ? "uploaded-image" : image.getOriginalFilename()
        );

        boolean published = detectionProcessingService.process(request);
        String message = published
                ? "Persona detectada por ONNX y evento publicado"
                : "Persona detectada por ONNX pero bloqueada por reglas (threshold/cooldown)";

        return ResponseEntity.ok(new OnnxImageDetectionResponse(
                true,
                true,
                result.confidence(),
                published,
                message
        ));
    }
}
