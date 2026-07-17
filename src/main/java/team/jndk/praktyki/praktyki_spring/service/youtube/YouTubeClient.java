package team.jndk.praktyki.praktyki_spring.service.youtube;

import java.util.List;
import java.util.Optional;

public interface YouTubeClient {

    Optional<YTApiChannel> getChannelInfo(String channelId) throws Exception;

    List<String> listVideoIdsForChannel(String channelId, long maxResults) throws Exception;

    YTApiVideo getVideoDetails(String videoId) throws Exception;

    class YTApiChannel {
        public final String title;
        public final long subscriberCount;

        public YTApiChannel(String title, long subscriberCount) {
            this.title = title;
            this.subscriberCount = subscriberCount;
        }
    }

    class YTApiVideo {
        public final String videoId;
        public final String title;
        public final String channelId;
        public final Integer likeCount;
        public final Integer commentCount;
        public final Integer viewCount;

        public YTApiVideo(String videoId, String title, String channelId, Integer likeCount, Integer commentCount, Integer viewCount) {
            this.videoId = videoId;
            this.title = title;
            this.channelId = channelId;
            this.likeCount = likeCount;
            this.commentCount = commentCount;
            this.viewCount = viewCount;
        }

        public YTApiVideo(String videoId, String title, Integer likeCount, Integer commentCount, Integer viewCount) {
            this(videoId, title, null, likeCount, commentCount, viewCount);
        }
    }
}

