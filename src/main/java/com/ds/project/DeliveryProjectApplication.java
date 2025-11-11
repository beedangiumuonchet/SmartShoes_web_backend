package com.ds.project;

import com.ds.project.application.configs.MomoConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(MomoConfig.class)
public class DeliveryProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeliveryProjectApplication.class, args);
    }
}
