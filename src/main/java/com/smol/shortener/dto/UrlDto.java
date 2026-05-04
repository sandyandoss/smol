package com.smol.shortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

public class UrlDto {

    @Data
    //This is what the client sends when creating a short URL
    public static class CreateRequest {

        @NotBlank(message = "URL must not be blank")
        @URL(message = "Must be a valid URL (include http:// or https://)")
        @Size(max = 2048, message = "URL must be 2048 characters or fewer")
        private String originalUrl;

        @Size(min = 3, max = 50, message = "Custom alias must be between 3 and 50 characters")
        //to prrevent SQL injection attempts ..
        @Pattern(regexp = "^[a-zA-Z0-9_-]*$",
                message = "Custom alias can only contain letters, numbers, hyphens, and underscores")
        private String customAlias;

        private LocalDateTime expiresAt;
    }

    @Data
    public static class CreateResponse {
        private String shortCode;
        private String shortUrl;
        private String originalUrl;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private String qrCodeUrl;
    }

    @Data
    public static class AnalyticsResponse {
        private String shortCode;
        private String shortUrl;
        private String originalUrl;
        private Long totalClicks;
        private Long clicksLast24Hours;
        private Long clicksLast7Days;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private boolean expired;
    }
}