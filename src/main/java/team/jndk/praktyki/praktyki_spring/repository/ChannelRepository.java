package team.jndk.praktyki.praktyki_spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team.jndk.praktyki.praktyki_spring.model.data.Channel;

import java.nio.channels.Channels;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Integer> {
}
