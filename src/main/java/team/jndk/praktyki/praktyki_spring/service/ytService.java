package team.jndk.praktyki.praktyki_spring.service;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import team.jndk.praktyki.praktyki_spring.model.data.YTVideo;
import team.jndk.praktyki.praktyki_spring.repository.ChannelRepository;
import team.jndk.praktyki.praktyki_spring.repository.VideoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class ytService {

    private final YouTube youtube;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Value("${youtube.api.key}")
    private String apiKey;



    @Autowired
    public ytService() {
        youtube = new YouTube.Builder(
                new NetHttpTransport(), new JacksonFactory(), request -> {
        })
                .setApplicationName("ytDataScanner")
                .build();
    }


    public void fetchAndSaveVideos(String channelId) {
        try {
            YouTube.Channels.List channelRequest = youtube.channels().list("snippet");
            channelRequest.setKey(apiKey);
            channelRequest.setId(channelId);

            ChannelListResponse channelResponse = channelRequest.execute();
            List<Channel> channels = channelResponse.getItems();
            Channel channel = channels.get(0);
            String channelTitle = channel.getSnippet().getTitle();
            team.jndk.praktyki.praktyki_spring.model.data.Channel chan = new team.jndk.praktyki.praktyki_spring.model.data.Channel(channelTitle,channelId);

            if (!channels.isEmpty()) {
                channelRepository.save(chan);
            } else {
                System.out.println("Nie znaleziono kanału o podanym ID.");
            }


            YouTube.Search.List searchRequest = youtube.search().list("id");
            searchRequest.setKey(apiKey);
            searchRequest.setChannelId(channelId);
            searchRequest.setType("video");
            searchRequest.setMaxResults(50L);


            SearchListResponse searchResponse = searchRequest.execute();
            List<SearchResult> searchResults = searchResponse.getItems();

            for (SearchResult searchResult : searchResults) {
                String videoId = searchResult.getId().getVideoId();
                YouTube.Videos.List videoRequest = youtube.videos().list("snippet,statistics");
                videoRequest.setKey(apiKey);
                videoRequest.setId(videoId);

                VideoListResponse videoResponse = videoRequest.execute();
                List<Video> videos = videoResponse.getItems();

                if (!videos.isEmpty()) {
                    Video video = videos.get(0);
                    VideoStatistics statistics = video.getStatistics();

                    int likeCount = statistics.getLikeCount().intValue();
                    int commentCount = statistics.getCommentCount().intValue();
                    int viewCount = statistics.getViewCount().intValue();
                    LocalDateTime now = LocalDateTime.now();
                    long date = now.toInstant(ZoneOffset.UTC).toEpochMilli();

                    YTVideo ytVideo = new YTVideo(video.getSnippet().getTitle(),videoId , likeCount, commentCount, viewCount, date);
                    ytVideo.setChannel(chan);
                    videoRepository.save(ytVideo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}