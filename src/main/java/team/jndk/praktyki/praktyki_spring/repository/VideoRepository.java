package team.jndk.praktyki.praktyki_spring.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import team.jndk.praktyki.praktyki_spring.model.data.YTVideo;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<YTVideo, Integer> {
    List<YTVideo> findByGoogleIdOrderByScannedDateDesc(String googleVidId);
    Optional<YTVideo> findByGoogleId(String googleVidId);
    Optional<YTVideo> findTopByGoogleIdOrderByScannedDateDesc(String googleVidId);

    // New analytics query methods - using actual field names from YTVideo entity
    @Query("SELECT v FROM YTVideo v WHERE v.channel.googleId = :chanId")
    Page<YTVideo> findByChanId(@Param("chanId") String chanId, Pageable pageable);

    @Query("SELECT v FROM YTVideo v WHERE v.channel.googleId = :chanId AND v.scannedDate >= :fromDate AND v.scannedDate < :toDate")
    Page<YTVideo> findByChannelAndDateRange(@Param("chanId") String chanId, @Param("fromDate") long fromDate, @Param("toDate") long toDate, Pageable pageable);

    Page<YTVideo> findByScannedDateBetween(long fromDate, long toDate, Pageable pageable);

    @Query("SELECT MAX(v.scannedDate) FROM YTVideo v")
    Optional<Long> findLatestScannedDate();

    @Modifying
    @Transactional
    @Query("DELETE FROM YTVideo v WHERE v.channel.googleId = :channelId")
    int deleteByChannelGoogleId(@Param("channelId") String channelId);
}
