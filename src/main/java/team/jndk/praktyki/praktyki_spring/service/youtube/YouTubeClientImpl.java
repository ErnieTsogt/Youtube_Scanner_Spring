package team.jndk.praktyki.praktyki_spring.service.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class YouTubeClientImpl implements YouTubeClient {

    private final YouTube youtube;

    @Value("${youtube.api.key}")
    private String apiKey;

    public YouTubeClientImpl(YouTube youtube) {
        this.youtube = youtube;
    }

    @Override
    public Optional<YouTubeClient.YTApiChannel> getChannelInfo(String channelId) throws Exception {
        YouTube.Channels.List channelRequest = youtube.channels().list("snippet,statistics");
        channelRequest.setKey(apiKey);
        channelRequest.setId(channelId);

        ChannelListResponse channelResponse = channelRequest.execute();
        List<com.google.api.services.youtube.model.Channel> channels = channelResponse.getItems();
        if (channels == null || channels.isEmpty()) return Optional.empty();

        com.google.api.services.youtube.model.Channel ch = channels.get(0);
        String title = ch.getSnippet().getTitle();
        long subscriberCount = ch.getStatistics() != null && ch.getStatistics().getSubscriberCount() != null
                ? ch.getStatistics().getSubscriberCount().longValue()
                : 0L;

        return Optional.of(new YouTubeClient.YTApiChannel(title, subscriberCount));
    }

    @Override
    public List<String> listVideoIdsForChannel(String channelId, long maxResults) throws Exception {
        List<String> ids = new ArrayList<>();
        YouTube.Search.List searchRequest = youtube.search().list("id");
        searchRequest.setKey(apiKey);
        searchRequest.setChannelId(channelId);
        searchRequest.setType("video");
        searchRequest.setOrder("date");
        searchRequest.setMaxResults(Math.min(maxResults, 50L));

        SearchListResponse searchResponse = searchRequest.execute();
        List<SearchResult> searchResults = searchResponse.getItems();
        if (searchResults == null) return ids;
        for (SearchResult r : searchResults) {
            if (r.getId() != null && r.getId().getVideoId() != null) ids.add(r.getId().getVideoId());
        }
        return ids;
    }

    @Override
    public YTApiVideo getVideoDetails(String videoId) throws Exception {
        YouTube.Videos.List videoRequest = youtube.videos().list("snippet,statistics");
        videoRequest.setKey(apiKey);
        videoRequest.setId(videoId);
        VideoListResponse videoResponse = videoRequest.execute();
        List<Video> videos = videoResponse.getItems();
        if (videos == null || videos.isEmpty()) return null;
        Video v = videos.get(0);
        VideoStatistics stats = v.getStatistics();
        Integer likeCount = stats.getLikeCount() != null ? stats.getLikeCount().intValue() : 0;
        Integer commentCount = stats.getCommentCount() != null ? stats.getCommentCount().intValue() : 0;
        Integer viewCount = stats.getViewCount() != null ? stats.getViewCount().intValue() : 0;
        String channelId = v.getSnippet() != null ? v.getSnippet().getChannelId() : null;
        return new YTApiVideo(videoId, v.getSnippet().getTitle(), channelId, likeCount, commentCount, viewCount);
    }
}

