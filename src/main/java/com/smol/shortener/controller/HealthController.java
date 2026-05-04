package com.smol.shortener.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HealthController {

    private final DataSource dataSource;
    private final RedisTemplate<String, String> redisTemplate;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("service", "smol");
        status.put("timestamp", LocalDateTime.now());
        status.put("status", "UP");
        status.put("database", checkDatabase());
        status.put("redis", checkRedis());
        return ResponseEntity.ok(status);
    }

    private String checkDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(1) ? "UP" : "DOWN";
        } catch (Exception e) {
            return "DOWN — " + e.getMessage();
        }
    }

    private String checkRedis() {
        try {
            redisTemplate.opsForValue().set("health_check", "ok");
            return "UP";
        } catch (Exception e) {
            return "DOWN — " + e.getMessage();
        }
    }
}