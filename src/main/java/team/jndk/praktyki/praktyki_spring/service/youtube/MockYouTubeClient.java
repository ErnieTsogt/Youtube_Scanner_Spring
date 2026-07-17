package team.jndk.praktyki.praktyki_spring.service.youtube;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Profile("dev")
@Primary
public class MockYouTubeClient implements YouTubeClient {

    @Override
    public Optional<YouTubeClient.YTApiChannel> getChannelInfo(String channelId) {
        long mockSubscribers = Math.abs((long) channelId.hashCode() % 10_000_000L) + 100_000L;
        return Optional.of(new YouTubeClient.YTApiChannel("MockChannel-" + channelId, mockSubscribers));
    }

    @Override
    public List<String> listVideoIdsForChannel(String channelId, long maxResults) {
        List<String> ids = new ArrayList<>();
        ids.add(channelId + "-vid1");
        ids.add(channelId + "-vid2");
        return ids;
    }

    @Override
    public YTApiVideo getVideoDetails(String videoId) {
        // generate deterministic sample metrics based on videoId hash
        int views = Math.abs(videoId.hashCode() % 1000) + 100;
        int likes = Math.abs(videoId.hashCode() % 100) + 10;
        int comments = Math.abs(videoId.hashCode() % 20) + 1;
        String title = "Title for " + videoId;
        String syntheticChannelId = videoId != null && videoId.contains("-vid")
                ? videoId.substring(0, videoId.indexOf("-vid"))
                : "UC_MOCK_CHANNEL";
        return new YTApiVideo(videoId, title, syntheticChannelId, likes, comments, views);
    }
}
