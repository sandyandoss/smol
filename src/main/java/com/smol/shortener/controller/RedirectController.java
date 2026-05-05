package com.smol.shortener.controller;

import com.smol.shortener.service.UrlService;
import com.smol.shortener.service.impl.AnalyticsService;
import com.smol.shortener.service.impl.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RedirectController {

    private final UrlService urlService;
    private final AnalyticsService analyticsService;
    private final RateLimitService rateLimitService;

    private static final Set<String> RESERVED = Set.of(
            "index.html", "favicon.ico", "robots.txt",
            "css", "js", "images", "static", "api"
    );

    /**
     * Serve the frontend at root /
     */
    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> home() throws IOException {
        ClassPathResource resource = new ClassPathResource("static/index.html");
        String html = StreamUtils.copyToString(
                resource.getInputStream(),
                StandardCharsets.UTF_8
        );
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    /**
     * Handle short code redirects
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            HttpServletRequest request) {

        // Skip reserved paths
        if (RESERVED.contains(shortCode.toLowerCase())) {
            return ResponseEntity.notFound().build();
        }

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