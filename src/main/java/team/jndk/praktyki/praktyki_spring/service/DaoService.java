package team.jndk.praktyki.praktyki_spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import team.jndk.praktyki.praktyki_spring.model.data.Channel;
import team.jndk.praktyki.praktyki_spring.model.data.DataGenerator;
import team.jndk.praktyki.praktyki_spring.model.data.Video;
import team.jndk.praktyki.praktyki_spring.repository.ChannelRepository;
import team.jndk.praktyki.praktyki_spring.repository.VideoRepository;

import java.util.List;

@Service
public class DaoService {

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private DataGenerator dataGenerator;

    @Value("${num_channels}")
    private Integer numChannels;

    public List<Channel> getAllChannels() {
        return channelRepository.findAll();
    }

    public List<Video> getAllVideos() {
        return videoRepository.findAll();
    }

    public void startScan() {
        List<Channel> channels = dataGenerator.generateChannels(numChannels);
        channelRepository.saveAll(channels);
        channels.forEach(channel -> videoRepository.saveAll(channel.getVideos()));
    };

}