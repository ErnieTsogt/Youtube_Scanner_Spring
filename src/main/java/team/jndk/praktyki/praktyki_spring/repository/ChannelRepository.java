package team.jndk.praktyki.praktyki_spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import team.jndk.praktyki.praktyki_spring.model.data.Channel;

import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Integer> {
    Optional<Channel> findByGoogleId(String googleId);

    @Transactional
    long deleteByGoogleId(String googleId);
}
