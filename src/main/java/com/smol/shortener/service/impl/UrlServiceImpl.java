package com.smol.shortener.service.impl;

import com.smol.shortener.config.AppProperties;
import com.smol.shortener.dto.UrlDto;
import com.smol.shortener.entity.Url;
import com.smol.shortener.exception.SmolExceptions;
import com.smol.shortener.repository.AccessLogRepository;
import com.smol.shortener.repository.UrlRepository;
import com.smol.shortener.service.UrlService;
import com.smol.shortener.util.Base62Encoder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;
    private final AccessLogRepository accessLogRepository;
    private final Base62Encoder base62Encoder;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public UrlDto.CreateResponse createShortUrl(UrlDto.CreateRequest request) {
        if (request.getCustomAlias() != null && !request.getCustomAlias().isBlank()) {
            return createWithCustomAlias(request);
        }
        return createWithGeneratedCode(request);
    }

    private UrlDto.CreateResponse createWithGeneratedCode(UrlDto.CreateRequest request) {
        // Step 1: Save with temp short code to get the DB-generated ID
        Url url = Url.builder()
                .originalUrl(request.getOriginalUrl())
                .shortCode("temp_" + System.currentTimeMillis())
                .expiresAt(request.getExpiresAt())
                .build();

        Url savedUrl = urlRepository.save(url);

        // Step 2: Now encode the real ID into Base62 and update
        String shortCode = base62Encoder.encode(savedUrl.getId());
        savedUrl.setShortCode(shortCode);
        urlRepository.save(savedUrl);

        log.info("Created short URL: {} → {}", shortCode, request.getOriginalUrl());
        return buildCreateResponse(savedUrl, shortCode);
    }

    private UrlDto.CreateResponse createWithCustomAlias(UrlDto.CreateRequest request) {
        String alias = request.getCustomAlias();

        if (urlRepository.existsByCustomAlias(alias)) {
            throw new SmolExceptions.AliasAlreadyExistsException(alias);
        }
        if (urlRepository.existsByShortCode(alias)) {
            throw new SmolExceptions.AliasAlreadyExistsException(alias);
        }

        Url url = Url.builder()
                .originalUrl(request.getOriginalUrl())
                .shortCode(alias)
                .customAlias(alias)
                .expiresAt(request.getExpiresAt())
                .build();

        Url savedUrl = urlRepository.save(url);
        log.info("Created custom alias: {} → {}", alias, request.getOriginalUrl());
        return buildCreateResponse(savedUrl, alias);
    }

    @Override
    @Cacheable(value = "urls", key = "#shortCode", unless = "#result == null")
    public String resolveShortCode(String shortCode) {
        log.debug("Cache miss for shortCode: {} — querying DB", shortCode);

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new SmolExceptions.UrlNotFoundException(shortCode));

        if (url.isExpired()) {
            throw new SmolExceptions.UrlExpiredException(shortCode);
        }

        return url.getOriginalUrl();
    }

    @Override
    public UrlDto.AnalyticsResponse getAnalytics(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new SmolExceptions.UrlNotFoundException(shortCode));

        LocalDateTime now = LocalDateTime.now();
        long clicksLast24h = accessLogRepository.countByUrlIdSince(url.getId(), now.minusHours(24));
        long clicksLast7d  = accessLogRepository.countByUrlIdSince(url.getId(), now.minusDays(7));

        UrlDto.AnalyticsResponse response = new UrlDto.AnalyticsResponse();
        response.setShortCode(shortCode);
        response.setShortUrl(buildShortUrl(shortCode));
        response.setOriginalUrl(url.getOriginalUrl());
        response.setTotalClicks(url.getClickCount());
        response.setClicksLast24Hours(clicksLast24h);
        response.setClicksLast7Days(clicksLast7d);
        response.setCreatedAt(url.getCreatedAt());
        response.setExpiresAt(url.getExpiresAt());
        response.setExpired(url.isExpired());

        return response;
    }

    private UrlDto.CreateResponse buildCreateResponse(Url url, String shortCode) {
        UrlDto.CreateResponse response = new UrlDto.CreateResponse();
        response.setShortCode(shortCode);
        response.setShortUrl(buildShortUrl(shortCode));
        response.setOriginalUrl(url.getOriginalUrl());
        response.setCreatedAt(url.getCreatedAt());
        response.setExpiresAt(url.getExpiresAt());
        response.setQrCodeUrl(appProperties.getBaseUrl() + "/api/qr/" + shortCode);
        return response;
    }

    private String buildShortUrl(String shortCode) {
        return appProperties.getBaseUrl() + "/" + shortCode;
    }
}