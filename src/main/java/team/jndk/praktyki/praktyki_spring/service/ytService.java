package team.jndk.praktyki.praktyki_spring.service;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Value;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.jndk.praktyki.praktyki_spring.model.data.YTVideo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ytService {

    private final YouTube youtube;

    @Value("${youtube.api.key}")
    private String apiKey;
    @Autowired
    public ytService() {
        youtube = new YouTube.Builder(
                new NetHttpTransport(), new JacksonFactory(), request -> {})
                .setApplicationName("ytDataScanner")
                .build();
    }

    public List<YTVideo> fetchAndSaveVideos(Channel channel) {
        List<YTVideo> videos = new ArrayList<>();

        YouTube.Search.List searchRequest;
        try {
            searchRequest = youtube.search().list("id,snippet,statistics");
            searchRequest.setKey(apiKey);
            searchRequest.setChannelId(channel.getId());
            searchRequest.setType("video");
            searchRequest.setMaxResults(50L);


            SearchListResponse searchResponse = searchRequest.execute();
            List<SearchResult> searchResults = searchResponse.getItems();

            for (SearchResult searchResult : searchResults) {
                YTVideo video = new YTVideo(searchResult.getSnippet().getTitle(), searchResult.getId().getVideoId(),
                        100, 100, 1000, 10000000000L);
                ResourceId resourceId = searchResult.getId();
                video.setId(Integer.parseInt(resourceId.getVideoId()));
                videos.add(video);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return videos;
    }
}