package com.wateracademy.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class TrainerMatchingConfig {

    @Value("${trainer-matching.base-url}")
    private String baseUrl;

    @Value("${trainer-matching.connect-timeout-ms}")
    private int connectTimeoutMs;

    @Value("${trainer-matching.read-timeout-ms}")
    private int readTimeoutMs;

    @Bean
    public RestClient trainerMatchingClient() {
        var settings = ClientHttpRequestFactorySettings.DEFAULTS
            .withConnectTimeout(Duration.ofMillis(connectTimeoutMs))
            .withReadTimeout(Duration.ofMillis(readTimeoutMs));
        return RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(ClientHttpRequestFactories.get(settings))
            .build();
    }
}
