package com.monitoreo.notifier;

import com.monitoreo.notifier.notification.NotificationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(NotificationProperties.class)
public class NotifierServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotifierServiceApplication.class, args);
    }
}
