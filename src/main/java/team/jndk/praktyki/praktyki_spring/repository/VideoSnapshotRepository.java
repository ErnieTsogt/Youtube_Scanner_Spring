package team.jndk.praktyki.praktyki_spring.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import team.jndk.praktyki.praktyki_spring.model.data.VideoSnapshot;

import java.util.Optional;
import java.util.List;

@Repository
public interface VideoSnapshotRepository extends JpaRepository<VideoSnapshot, Long> {

    @Query("""
	    SELECT s
	    FROM VideoSnapshot s
	    JOIN s.video v
	    JOIN v.channel c
	    WHERE (:channelGoogleId IS NULL OR :channelGoogleId = '' OR LOWER(c.googleId) = LOWER(:channelGoogleId))
	      AND (:fromDate IS NULL OR s.snapshotDate >= :fromDate)
	      AND (:toDate IS NULL OR s.snapshotDate < :toDate)
	    """)
    Page<VideoSnapshot> findHistory(
	    @Param("channelGoogleId") String channelGoogleId,
	    @Param("fromDate") Long fromDate,
	    @Param("toDate") Long toDate,
	    Pageable pageable
    );

	@Modifying
	@Transactional
	@Query("DELETE FROM VideoSnapshot s WHERE s.video.channel.googleId = :channelGoogleId")
	int deleteByChannelGoogleId(@Param("channelGoogleId") String channelGoogleId);

	@Query("""
		SELECT s
		FROM VideoSnapshot s
		JOIN s.video v
		JOIN v.channel c
		WHERE (:channelGoogleId IS NULL OR :channelGoogleId = '' OR LOWER(c.googleId) = LOWER(:channelGoogleId))
		  AND (:fromDate IS NULL OR s.snapshotDate >= :fromDate)
		ORDER BY s.snapshotDate ASC
		""")
	List<VideoSnapshot> findForTrends(
		@Param("channelGoogleId") String channelGoogleId,
		@Param("fromDate") Long fromDate
	);

	Optional<VideoSnapshot> findByVideo_GoogleIdAndSnapshotDate(String videoGoogleId, long snapshotDate);

	List<VideoSnapshot> findTop4ByVideo_GoogleIdOrderBySnapshotDateDesc(String videoGoogleId);
}

