package com.smol.shortener.exception;

import com.smol.shortener.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


//global error handler for the whole app, catches exception automatically and returns json
@RestControllerAdvice
@Slf4j //lombok logging gives log.info()  log.warn()  log.error()
public class GlobalExceptionHandler {


    //404 Not Found
    @ExceptionHandler(SmolExceptions.UrlNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            SmolExceptions.UrlNotFoundException ex,
            HttpServletRequest request) {

        log.warn("URL not found: {}", ex.getMessage());
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }
//returns 404 Not Found or 410: exist but expired
    @ExceptionHandler(SmolExceptions.UrlExpiredException.class)
    public ResponseEntity<ApiError> handleExpired(
            SmolExceptions.UrlExpiredException ex,
            HttpServletRequest request) {

        log.info("Expired URL access attempt: {}", ex.getMessage());
        return buildError(HttpStatus.GONE, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(SmolExceptions.AliasAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleAliasConflict(
            SmolExceptions.AliasAlreadyExistsException ex,
            HttpServletRequest request) {

        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }
//returns 429 -> standard for rate limiting
    @ExceptionHandler(SmolExceptions.RateLimitExceededException.class)
    public ResponseEntity<ApiError> handleRateLimit(
            SmolExceptions.RateLimitExceededException ex,
            HttpServletRequest request) {

        log.warn("Rate limit exceeded: {}", ex.getMessage());
        return buildError(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(SmolExceptions.QrCodeGenerationException.class)
    public ResponseEntity<ApiError> handleQrError(
            SmolExceptions.QrCodeGenerationException ex,
            HttpServletRequest request) {

        log.error("QR code generation error", ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("One or more fields failed validation. See 'fieldErrors' for details.")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI()
        );
    }

    private ResponseEntity<ApiError> buildError(HttpStatus status, String message, String path) {
        ApiError error = ApiError.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(status).body(error);
    }
}