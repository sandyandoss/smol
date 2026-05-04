package com.smol.shortener.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.smol.shortener.exception.SmolExceptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class QrCodeService {

    private static final int DEFAULT_SIZE = 300;
    private static final String FORMAT = "PNG";

    public byte[] generateQrCode(String url, int size) {
        int imageSize = size > 0 ? size : DEFAULT_SIZE;

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 2);

        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(url, BarcodeFormat.QR_CODE, imageSize, imageSize, hints);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, FORMAT, outputStream);

            log.debug("Generated {}x{} QR code for: {}", imageSize, imageSize, url);
            return outputStream.toByteArray();

        } catch (WriterException | IOException e) {
            throw new SmolExceptions.QrCodeGenerationException(
                    "Could not encode URL into QR code: " + url, e
            );
        }
    }

    public byte[] generateQrCode(String url) {
        return generateQrCode(url, DEFAULT_SIZE);
    }
}