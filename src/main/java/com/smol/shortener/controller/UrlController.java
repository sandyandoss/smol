package com.smol.shortener.controller;

import com.smol.shortener.dto.UrlDto;
import com.smol.shortener.service.UrlService;
import com.smol.shortener.service.impl.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;
    private final RateLimitService rateLimitService;

    @PostMapping("/urls")
    public ResponseEntity<UrlDto.CreateResponse> createShortUrl(
            @Valid @RequestBody UrlDto.CreateRequest request,
            HttpServletRequest httpRequest) {

        rateLimitService.checkRateLimit(getClientIp(httpRequest));
        UrlDto.CreateResponse response = urlService.createShortUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/urls/{shortCode}/analytics")
    public ResponseEntity<UrlDto.AnalyticsResponse> getAnalytics(
            @PathVariable String shortCode) {

        return ResponseEntity.ok(urlService.getAnalytics(shortCode));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}