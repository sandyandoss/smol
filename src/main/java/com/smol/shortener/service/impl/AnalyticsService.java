package com.smol.shortener.service.impl;

import com.smol.shortener.entity.AccessLog;
import com.smol.shortener.repository.AccessLogRepository;
import com.smol.shortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final AccessLogRepository accessLogRepository;
    private final UrlRepository urlRepository;

    @Async("analyticsExecutor")
    @Transactional
    public void logAccess(String shortCode, String ipAddress, String userAgent, String referer) {
        try {
            urlRepository.findByShortCode(shortCode).ifPresent(url -> {
                urlRepository.incrementClickCount(url.getId());

                AccessLog accessLog = AccessLog.builder()
                        .url(url)
                        .ipAddress(ipAddress)
                        .userAgent(truncate(userAgent, 512))
                        .referer(truncate(referer, 512))
                        .build();

                accessLogRepository.save(accessLog);
            });
        } catch (Exception e) {
            log.error("Failed to log access for shortCode {}: {}", shortCode, e.getMessage());
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}