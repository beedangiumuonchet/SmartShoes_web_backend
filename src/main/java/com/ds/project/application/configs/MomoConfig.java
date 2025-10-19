package com.ds.project.application.configs;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "momo")
public class MomoConfig {
    private String partnerCode;
    private String accessKey;
    private String secretKey;
    private String createEndpoint;
    private String queryEndpoint;
    private String returnUrl;
    private String ipnUrl;
    private String requestType;

    @PostConstruct
    public void checkLoaded() {
        System.out.println("[MOMO CONFIG LOADED]");
        System.out.println("partnerCode=" + partnerCode);
        System.out.println("ipnUrl=" + ipnUrl);
    }
}
