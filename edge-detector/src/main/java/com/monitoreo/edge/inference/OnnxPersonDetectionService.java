package com.monitoreo.edge.inference;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.monitoreo.edge.config.OnnxProperties;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import javax.imageio.ImageIO;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OnnxPersonDetectionService {

    private final OnnxProperties onnxProperties;
    private final OrtEnvironment environment;
    private final OrtSession session;
    private final String inputName;
    private final MeterRegistry meterRegistry;
    private final Timer inferenceTimer;
    private final Counter inferenceRequestsCounter;
    private final Counter inferencePersonDetectedCounter;
    private final Counter inferenceNoPersonCounter;
    private final Counter inferenceErrorsCounter;

    public OnnxPersonDetectionService(OnnxProperties onnxProperties, MeterRegistry meterRegistry) {
        this.onnxProperties = onnxProperties;
        this.meterRegistry = meterRegistry;
        this.inferenceTimer = Timer.builder("edge.onnx.inference.duration")
                .description("Duracion de inferencia ONNX para deteccion de persona")
                .register(meterRegistry);
        this.inferenceRequestsCounter = meterRegistry.counter("edge.onnx.inference.requests");
        this.inferencePersonDetectedCounter = meterRegistry.counter("edge.onnx.inference.result", "person_detected", "true");
        this.inferenceNoPersonCounter = meterRegistry.counter("edge.onnx.inference.result", "person_detected", "false");
        this.inferenceErrorsCounter = meterRegistry.counter("edge.onnx.inference.errors");

        if (!onnxProperties.enabled()) {
            this.environment = null;
            this.session = null;
            this.inputName = null;
            return;
        }

        try {
            if (onnxProperties.modelPath() == null || onnxProperties.modelPath().isBlank()) {
                throw new IllegalStateException("app.onnx.model-path no configurado");
            }

            Path modelPath = Path.of(onnxProperties.modelPath());
            if (!Files.exists(modelPath)) {
                throw new IllegalStateException("Modelo ONNX no existe en: " + modelPath);
            }

            this.environment = OrtEnvironment.getEnvironment();
            OrtSession.SessionOptions options = new OrtSession.SessionOptions();
            this.session = environment.createSession(modelPath.toString(), options);
            this.inputName = session.getInputNames().iterator().next();
        } catch (OrtException e) {
            throw new IllegalStateException("No se pudo inicializar ONNX Runtime", e);
        }
    }

    public boolean isEnabled() {
        return onnxProperties.enabled();
    }

    public PersonDetectionResult detectPerson(MultipartFile imageFile) {
        if (!onnxProperties.enabled()) {
            throw new IllegalStateException("ONNX deshabilitado. Activa app.onnx.enabled=true");
        }

        inferenceRequestsCounter.increment();
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            BufferedImage original = ImageIO.read(imageFile.getInputStream());
            if (original == null) {
                throw new IllegalArgumentException("Archivo no valido como imagen");
            }

            int width = onnxProperties.inputWidth();
            int height = onnxProperties.inputHeight();

            BufferedImage resized = resizeImage(original, width, height);
            float[] chw = toNormalizedChw(resized);
            long[] shape = new long[]{1, 3, height, width};

            try (OnnxTensor inputTensor = OnnxTensor.createTensor(environment, FloatBuffer.wrap(chw), shape);
                 OrtSession.Result result = session.run(Map.of(inputName, inputTensor))) {

                Object outputValue = result.get(0).getValue();
                double bestPersonScore = extractBestPersonScore(outputValue, onnxProperties.personClassIndex());
                boolean personDetected = bestPersonScore > 0;
                if (personDetected) {
                    inferencePersonDetectedCounter.increment();
                } else {
                    inferenceNoPersonCounter.increment();
                }
                return new PersonDetectionResult(personDetected, bestPersonScore);
            }
        } catch (IOException e) {
            inferenceErrorsCounter.increment();
            throw new IllegalStateException("No se pudo leer la imagen para inferencia", e);
        } catch (OrtException e) {
            inferenceErrorsCounter.increment();
            throw new IllegalStateException("Error ejecutando inferencia ONNX", e);
        } finally {
            sample.stop(inferenceTimer);
        }
    }

    private double extractBestPersonScore(Object outputValue, int personClassIndex) {
        if (outputValue instanceof float[][][] output3d) {
            // Formato comun YOLOv8 ONNX: [1][84][8400]
            if (output3d.length == 0) {
                return 0;
            }
            float[][] channelsByDetections = output3d[0];
            if (channelsByDetections.length <= 4 + personClassIndex) {
                return 0;
            }

            float[] personScores = channelsByDetections[4 + personClassIndex];
            double max = 0;
            for (float score : personScores) {
                if (score > max) {
                    max = score;
                }
            }
            return max;
        }

        if (outputValue instanceof float[][] output2d) {
            // Fallback generico: [N][attributes], score en columna 4
            double max = 0;
            for (float[] row : output2d) {
                if (row.length > 4 && row[4] > max) {
                    max = row[4];
                }
            }
            return max;
        }

        return 0;
    }

    private BufferedImage resizeImage(BufferedImage source, int targetWidth, int targetHeight) {
        BufferedImage output = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = output.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return output;
    }

    private float[] toNormalizedChw(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        float[] data = new float[3 * width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                float r = ((rgb >> 16) & 0xff) / 255.0f;
                float g = ((rgb >> 8) & 0xff) / 255.0f;
                float b = (rgb & 0xff) / 255.0f;

                int index = y * width + x;
                data[index] = r;
                data[width * height + index] = g;
                data[2 * width * height + index] = b;
            }
        }

        return data;
    }
}
