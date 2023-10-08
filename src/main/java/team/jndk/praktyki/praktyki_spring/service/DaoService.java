package team.jndk.praktyki.praktyki_spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import team.jndk.praktyki.praktyki_spring.model.data.Channel;
import team.jndk.praktyki.praktyki_spring.model.data.DataGenerator;
import team.jndk.praktyki.praktyki_spring.model.data.YTVideo;
import team.jndk.praktyki.praktyki_spring.repository.ChannelRepository;
import team.jndk.praktyki.praktyki_spring.repository.VideoRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public List<YTVideo> getAllVideos() {
        List<YTVideo> allVideos = videoRepository.findAll();

        Map<String, List<YTVideo>> groupedVideos = allVideos.stream()
                .collect(Collectors.groupingBy(video -> video.getTitle() + "-" + video.getGoogleId()));

        List<YTVideo> newestVideos = groupedVideos.values().stream()
                .map(videoList -> videoList.stream()
                        .max(Comparator.comparingLong(YTVideo::getScannedDate))
                        .orElse(null))
                .collect(Collectors.toList());

        return newestVideos;
    }

    public void startScan() {
        List<Channel> channels = dataGenerator.generateChannels(numChannels);
        channelRepository.saveAll(channels);
        channels.forEach(channel -> videoRepository.saveAll(channel.getYTVideos()));
    };

}