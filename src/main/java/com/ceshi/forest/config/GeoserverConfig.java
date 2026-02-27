package com.ceshi.forest.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Geoserver配置类
 */
@Getter
@Configuration
public class GeoserverConfig {

    @Value("${geoserver.url:http://localhost:8080/geoserver}")
    private String geoserverUrl;

    @Value("${geoserver.username:admin}")
    private String geoserverUsername;

    @Value("${geoserver.password:geoserver}")
    private String geoserverPassword;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}