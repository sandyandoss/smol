package com.smol.shortener.service;

import com.smol.shortener.dto.UrlDto;

public interface UrlService {

    UrlDto.CreateResponse createShortUrl(UrlDto.CreateRequest request);

    String resolveShortCode(String shortCode);

    UrlDto.AnalyticsResponse getAnalytics(String shortCode);
}