package com.smol.shortener.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


//This entity = one click event
//
//Every time someone opens:
//smol.ly/abc123
//
//You save:
//
//when it happened
//who accessed it
//from where
//using what device


@Entity
@Table(name = "access_logs", indexes = {
        @Index(name = "idx_access_url_id", columnList = "url_id"),
        @Index(name = "idx_access_timestamp", columnList = "timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) //Many analytics records → belong to one URL + Don’t load the URL data unless needed
    @JoinColumn(name = "url_id", nullable = false)
    private Url url;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "ip_address", length = 45)  //Stores user’s IP
    private String ipAddress;

    @Column(name = "user_agent", length = 512) //Info about browser/device example: chrome on windows aw safari on apple
    private String userAgent;

    @Column(name = "referer", length = 512) //Where the user came from ex: google facebook ...
    private String referer;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}