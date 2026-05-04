package com.smol.shortener.controller;

import com.smol.shortener.service.UrlService;
import com.smol.shortener.service.impl.AnalyticsService;
import com.smol.shortener.service.impl.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RedirectController {

    private final UrlService urlService;
    private final AnalyticsService analyticsService;
    private final RateLimitService rateLimitService;

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            HttpServletRequest request) {

        String clientIp = getClientIp(request);
        rateLimitService.checkRateLimit(clientIp);

        String originalUrl = urlService.resolveShortCode(shortCode);

        analyticsService.logAccess(
                shortCode,
                clientIp,
                request.getHeader("User-Agent"),
                request.getHeader("Referer")
        );

        log.debug("Redirecting {} → {}", shortCode, originalUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(originalUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}