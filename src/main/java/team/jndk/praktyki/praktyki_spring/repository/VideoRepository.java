package team.jndk.praktyki.praktyki_spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team.jndk.praktyki.praktyki_spring.model.data.YTVideo;

@Repository
public interface VideoRepository extends JpaRepository<YTVideo, Integer> {
}
