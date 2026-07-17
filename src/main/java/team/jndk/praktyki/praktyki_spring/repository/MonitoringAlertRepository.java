package team.jndk.praktyki.praktyki_spring.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import team.jndk.praktyki.praktyki_spring.model.data.AlertType;
import team.jndk.praktyki.praktyki_spring.model.data.MonitoringAlert;

import java.util.List;

@Repository
public interface MonitoringAlertRepository extends JpaRepository<MonitoringAlert, Long> {

    @Query("""
            SELECT a
            FROM MonitoringAlert a
            LEFT JOIN FETCH a.video v
            LEFT JOIN FETCH a.channel c
            WHERE (:channelGoogleId IS NULL OR :channelGoogleId = '' OR LOWER(c.googleId) = LOWER(:channelGoogleId))
              AND (:acknowledged IS NULL OR a.acknowledged = :acknowledged)
            ORDER BY a.detectedAt DESC
            """)
    List<MonitoringAlert> findRecentAlerts(
            @Param("channelGoogleId") String channelGoogleId,
            @Param("acknowledged") Boolean acknowledged,
            Pageable pageable
    );

        @Modifying
        @Transactional
        @Query("DELETE FROM MonitoringAlert a WHERE a.channel.googleId = :channelGoogleId")
        int deleteByChannelGoogleId(@Param("channelGoogleId") String channelGoogleId);

    boolean existsByVideo_IdAndAlertTypeAndDetectedAtGreaterThanEqual(Integer videoId, AlertType alertType, long detectedAt);
}