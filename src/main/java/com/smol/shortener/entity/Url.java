//entity = classes that represent database tables


package com.smol.shortener.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity //This class = a table in the database
@Table(name = "urls", indexes = {
        @Index(name = "idx_short_code", columnList = "short_code", unique = true) //unique = true → no duplicates allowed
})

@Data //→ getters, setters, toString
@Builder //allows creating objects
@NoArgsConstructor //empty one → required by JPA
@AllArgsConstructor //for testing

//This class represents one row in the urls table
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //IDENTITY → database auto-increments it
    private Long id;

    @Column(name = "original_url", nullable = false, length = 2048) //nullable = false → must exist
    private String originalUrl;

    @Column(name = "short_code", nullable = false, unique = true, length = 20)
    private String shortCode;

    @Column(name = "custom_alias", length = 50)
    private String customAlias;

    @Column(name = "created_at", nullable = false, updatable = false) //updatable = false → can’t change later
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "click_count", nullable = false)
    @Builder.Default
    private Long clickCount = 0L;

//@PrePersist to automatically set creation time.
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}