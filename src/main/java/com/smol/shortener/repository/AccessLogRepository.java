//Repositories = the layer that talks to the database

package com.smol.shortener.repository;

import com.smol.shortener.entity.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {

    List<AccessLog> findByUrlId(Long urlId);

    long countByUrlId(Long urlId);

    @Query("SELECT COUNT(a) FROM AccessLog a WHERE a.url.id = :urlId AND a.timestamp >= :since")
    long countByUrlIdSince(@Param("urlId") Long urlId, @Param("since") LocalDateTime since);
}