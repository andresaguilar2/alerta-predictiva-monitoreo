package com.monitoreo.notifier;

import com.monitoreo.notifier.camera.CameraHealthProperties;
import com.monitoreo.notifier.notification.NotificationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
        NotificationProperties.class,
        CameraHealthProperties.class
})
public class NotifierServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotifierServiceApplication.class, args);
    }
}
