package com.smol.shortener.controller;

import com.smol.shortener.config.AppProperties;
import com.smol.shortener.service.impl.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
public class QrCodeController {

    private final QrCodeService qrCodeService;
    private final AppProperties appProperties;

    @GetMapping(value = "/{shortCode}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCode(
            @PathVariable String shortCode,
            @RequestParam(defaultValue = "300") int size) {

        int clampedSize = Math.min(Math.max(size, 100), 1000);
        String shortUrl = appProperties.getBaseUrl() + "/" + shortCode;
        byte[] qrBytes = qrCodeService.generateQrCode(shortUrl, clampedSize);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrBytes);
    }
}