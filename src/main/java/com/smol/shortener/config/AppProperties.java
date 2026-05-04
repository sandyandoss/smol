package com.smol.shortener.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
//Makes this class a Spring bean
//→ Spring can inject it anywhere by @Bean
@ConfigurationProperties(prefix = "app") //to read values from application.yml that start with app
@Data
public class AppProperties {


    //fields to:
    //build short url
    private String baseUrl;
    //nest config object
    private RateLimit rateLimit = new RateLimit();
    private Cache cache = new Cache();

    @Data
    public static class RateLimit {
        private int maxRequests = 100;
        private int windowSeconds = 60;
    }

    @Data
    public static class Cache {
        private int urlTtlSeconds = 3600;
    }
}