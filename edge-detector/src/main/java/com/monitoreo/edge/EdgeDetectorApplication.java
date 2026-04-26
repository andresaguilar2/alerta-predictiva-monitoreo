package com.monitoreo.edge;

import com.monitoreo.edge.config.DetectionRulesProperties;
import com.monitoreo.edge.config.NotifierProperties;
import com.monitoreo.edge.config.OnnxProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({DetectionRulesProperties.class, NotifierProperties.class, OnnxProperties.class})
public class EdgeDetectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(EdgeDetectorApplication.class, args);
	}

}
