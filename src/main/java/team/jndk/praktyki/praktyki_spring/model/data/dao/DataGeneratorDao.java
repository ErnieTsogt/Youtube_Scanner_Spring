package team.jndk.praktyki.praktyki_spring.model.data.dao;

import org.springframework.stereotype.Component;
import team.jndk.praktyki.praktyki_spring.model.data.Channel;

import java.util.List;
@Component
public interface DataGeneratorDao {
    void saveChannels(List<Channel> channels);
}
