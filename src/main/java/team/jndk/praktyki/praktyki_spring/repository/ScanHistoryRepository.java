package team.jndk.praktyki.praktyki_spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import team.jndk.praktyki.praktyki_spring.model.data.ScanHistory;

import java.util.Optional;

@Repository
public interface ScanHistoryRepository extends JpaRepository<ScanHistory, Long> {
	Optional<ScanHistory> findByChannel_GoogleIdAndScanDate(String channelGoogleId, long scanDate);

	@Modifying
	@Transactional
	@Query("DELETE FROM ScanHistory s WHERE s.channel.googleId = :channelGoogleId")
	int deleteByChannelGoogleId(@Param("channelGoogleId") String channelGoogleId);
}

