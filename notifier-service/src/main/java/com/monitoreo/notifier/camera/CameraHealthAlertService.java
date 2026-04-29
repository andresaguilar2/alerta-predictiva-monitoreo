package com.monitoreo.notifier.camera;

import com.monitoreo.notifier.notification.EmailNotificationGateway;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class CameraHealthAlertService {

    private static final Logger log = LoggerFactory.getLogger(CameraHealthAlertService.class);

    private final ObjectProvider<EmailNotificationGateway> emailGatewayProvider;

    public CameraHealthAlertService(ObjectProvider<EmailNotificationGateway> emailGatewayProvider) {
        this.emailGatewayProvider = emailGatewayProvider;
    }

    public void sendFailureAlert(String cameraId, String reason, Instant timestamp) {
        EmailNotificationGateway emailGateway = emailGatewayProvider.getIfAvailable();
        if (emailGateway == null) {
            log.warn("Alerta de camara no enviada por email: canal email deshabilitado. cameraId={} reason={}", cameraId, reason);
            return;
        }

        emailGateway.sendCameraFailureAlert(cameraId, reason, timestamp);
    }
}
