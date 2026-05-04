package com.smol.shortener.exception;
//special errors for my app
public class SmolExceptions {

    //extends RuntimeException → no need to manually handle everywhere
    public static class UrlNotFoundException extends RuntimeException {
        public UrlNotFoundException(String shortCode) {
            super("No URL found for short code: '" + shortCode + "'");
        }
    }

    public static class AliasAlreadyExistsException extends RuntimeException {
        public AliasAlreadyExistsException(String alias) {
            super("Custom alias '" + alias + "' is already taken. Please choose a different one.");
        }
    }

    public static class UrlExpiredException extends RuntimeException {
        public UrlExpiredException(String shortCode) {
            super("The short URL '" + shortCode + "' has expired and is no longer active.");
        }
    }
//example more than 100 requests/min → block user
    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException(String ip) {
            super("Rate limit exceeded for IP: " + ip + ". Please try again in a minute.");
        }
    }

    public static class QrCodeGenerationException extends RuntimeException {
        public QrCodeGenerationException(String message, Throwable cause) {
            super("QR code generation failed: " + message, cause);
        }
    }
}