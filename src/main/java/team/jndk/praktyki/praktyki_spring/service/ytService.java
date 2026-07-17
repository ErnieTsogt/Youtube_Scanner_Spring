package team.jndk.praktyki.praktyki_spring.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.jndk.praktyki.praktyki_spring.model.data.Channel;
import team.jndk.praktyki.praktyki_spring.model.data.YTVideo;
import team.jndk.praktyki.praktyki_spring.model.data.VideoSnapshot;
import team.jndk.praktyki.praktyki_spring.model.data.ScanHistory;
import team.jndk.praktyki.praktyki_spring.model.data.ScanStatus;
import team.jndk.praktyki.praktyki_spring.repository.ChannelRepository;
import team.jndk.praktyki.praktyki_spring.repository.VideoRepository;
import team.jndk.praktyki.praktyki_spring.repository.VideoSnapshotRepository;
import team.jndk.praktyki.praktyki_spring.repository.ScanHistoryRepository;
import team.jndk.praktyki.praktyki_spring.service.youtube.YouTubeClient;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
public class ytService {

    private static final Logger log = LoggerFactory.getLogger(ytService.class);

    private final YouTubeClient youTubeClient;
    private final VideoRepository videoRepository;
    private final ChannelRepository channelRepository;
    private final VideoSnapshotRepository videoSnapshotRepository;
    private final ScanHistoryRepository scanHistoryRepository;
    private final MonitoringAlertService monitoringAlertService;

    public ytService(YouTubeClient youTubeClient, VideoRepository videoRepository, ChannelRepository channelRepository, VideoSnapshotRepository videoSnapshotRepository, ScanHistoryRepository scanHistoryRepository, MonitoringAlertService monitoringAlertService) {
        this.youTubeClient = youTubeClient;
        this.videoRepository = videoRepository;
        this.channelRepository = channelRepository;
        this.videoSnapshotRepository = videoSnapshotRepository;
        this.scanHistoryRepository = scanHistoryRepository;
        this.monitoringAlertService = monitoringAlertService;
    }

    @Transactional
    public void fetchAndSaveVideos(String channelId) {
        String resolvedChannelId = resolveGoogleChannelId(channelId);
        if (resolvedChannelId == null || resolvedChannelId.isBlank()) {
            throw new IllegalArgumentException("Nieprawidłowe Channel ID");
        }
        if (!resolvedChannelId.startsWith("UC")) {
            throw new IllegalArgumentException("Podaj poprawne YouTube Channel ID (format UC...)");
        }

        long now = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
        ScanHistory history = null;
        try {
            Optional<YouTubeClient.YTApiChannel> channelInfo = youTubeClient.getChannelInfo(resolvedChannelId);
            if (channelInfo.isEmpty()) {
                log.warn("Nie znaleziono kanału o podanym ID: {}", channelId);
                history = new ScanHistory(null, now, ScanStatus.FAILED, "Channel not found", 0);
                scanHistoryRepository.save(history);
                throw new IllegalArgumentException("Nie znaleziono kanału o podanym ID: " + channelId);
            }

            String title = channelInfo.get().title;
            long subscribers = channelInfo.get().subscriberCount;

            Optional<team.jndk.praktyki.praktyki_spring.model.data.Channel> existing = channelRepository.findByGoogleId(resolvedChannelId);
            team.jndk.praktyki.praktyki_spring.model.data.Channel chan;
            if (existing.isPresent()) {
                chan = existing.get();
                chan.setSubscribers(subscribers);
                channelRepository.save(chan);
            } else {
                chan = new team.jndk.praktyki.praktyki_spring.model.data.Channel(title, resolvedChannelId, subscribers);
                channelRepository.save(chan);
            }

            List<String> videoIds = youTubeClient.listVideoIdsForChannel(resolvedChannelId, 50);
            if (videoIds == null || videoIds.isEmpty()) {
                log.info("Brak wyników wyszukiwania dla kanału {}", resolvedChannelId);
                history = new ScanHistory(chan, now, ScanStatus.PARTIAL, "No videos found", 0);
                scanHistoryRepository.save(history);
                return;
            }

            int savedCount = 0;
            for (String videoId : videoIds) {
                try {
                    YouTubeClient.YTApiVideo apiVideo = youTubeClient.getVideoDetails(videoId);
                    if (apiVideo == null) continue;

                    YTVideo ytVideo = videoRepository.findTopByGoogleIdOrderByScannedDateDesc(apiVideo.videoId)
                            .map(existingVideo -> {
                                if (isSameDay(existingVideo.getScannedDate(), now)) {
                                    existingVideo.setTitle(apiVideo.title);
                                    existingVideo.setLikes(apiVideo.likeCount);
                                    existingVideo.setComments(apiVideo.commentCount);
                                    existingVideo.setViews(apiVideo.viewCount);
                                    existingVideo.setScannedDate(now);
                                    existingVideo.setChannel(chan);
                                    return existingVideo;
                                }

                                YTVideo created = new YTVideo(apiVideo.title, apiVideo.videoId, apiVideo.likeCount, apiVideo.commentCount, apiVideo.viewCount, now);
                                created.setChannel(chan);
                                return created;
                            })
                            .orElseGet(() -> {
                                YTVideo created = new YTVideo(apiVideo.title, apiVideo.videoId, apiVideo.likeCount, apiVideo.commentCount, apiVideo.viewCount, now);
                                created.setChannel(chan);
                                return created;
                            });

                    // Save latest scan for the same day, but keep historical daily rows for trends
                    YTVideo saved = videoRepository.save(ytVideo);

                    // Create snapshot
                    VideoSnapshot snapshot = new VideoSnapshot(saved, now, saved.getViews(), saved.getLikes(), saved.getComments());
                    videoSnapshotRepository.save(snapshot);
                    monitoringAlertService.analyzeLatestSnapshot(saved);
                    savedCount++;

                } catch (Exception ex) {
                    log.warn("Błąd podczas przetwarzania video {}: {}", videoId, ex.getMessage());
                }
            }

            history = new ScanHistory(chan, now, ScanStatus.SUCCESS, "OK", savedCount);
            scanHistoryRepository.save(history);

        } catch (IllegalArgumentException e) {
            if (history == null) {
                history = new ScanHistory(null, now, ScanStatus.FAILED, e.getMessage(), 0);
                scanHistoryRepository.save(history);
            }
            throw e;
        } catch (Exception e) {
            log.error("Błąd podczas fetchAndSaveVideos dla {}: {}", channelId, e.getMessage(), e);
            if (history == null) history = new ScanHistory(null, now, ScanStatus.FAILED, e.getMessage(), 0);
            scanHistoryRepository.save(history);
            throw new IllegalStateException("Błąd podczas skanowania kanału: " + e.getMessage(), e);
        }

    }

    @Transactional
    public void fetchAndSaveSingleVideo(String channelIdRaw, String videoIdRaw) {
        String resolvedChannelId = resolveGoogleChannelId(channelIdRaw);
        if (resolvedChannelId == null || resolvedChannelId.isBlank() || !resolvedChannelId.startsWith("UC")) {
            throw new IllegalArgumentException("Podaj poprawne YouTube Channel ID (format UC...)");
        }

        String videoId = videoIdRaw != null ? videoIdRaw.trim() : "";
        if (videoId.isBlank()) {
            throw new IllegalArgumentException("Video ID jest wymagane");
        }

        long now = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
        ScanHistory history = null;

        try {
            Channel channel = channelRepository.findByGoogleId(resolvedChannelId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Kanał nie został jeszcze zeskanowany. Zeskanuj najpierw cały kanał: " + resolvedChannelId));

            Optional<YTVideo> existingForVideo = videoRepository.findTopByGoogleIdOrderByScannedDateDesc(videoId);
            if (existingForVideo.isEmpty() || existingForVideo.get().getChannel() == null ||
                    existingForVideo.get().getChannel().getGoogleId() == null ||
                    !existingForVideo.get().getChannel().getGoogleId().equalsIgnoreCase(resolvedChannelId)) {
                throw new IllegalArgumentException("Wybrany film nie należy do wcześniej zeskanowanego kanału: " + resolvedChannelId);
            }

            YouTubeClient.YTApiVideo apiVideo = youTubeClient.getVideoDetails(videoId);
            if (apiVideo == null) {
                history = new ScanHistory(channel, now, ScanStatus.FAILED, "Video not found", 0);
                scanHistoryRepository.save(history);
                throw new IllegalArgumentException("Nie znaleziono filmu o podanym ID: " + videoId);
            }

            if (apiVideo.channelId != null && !apiVideo.channelId.equalsIgnoreCase(resolvedChannelId)) {
                history = new ScanHistory(channel, now, ScanStatus.FAILED, "Video does not belong to channel", 0);
                scanHistoryRepository.save(history);
                throw new IllegalArgumentException("Film nie należy do wskazanego kanału");
            }

            YTVideo ytVideo = existingForVideo.get();
            ytVideo.setTitle(apiVideo.title);
            ytVideo.setLikes(apiVideo.likeCount);
            ytVideo.setComments(apiVideo.commentCount);
            ytVideo.setViews(apiVideo.viewCount);
            ytVideo.setScannedDate(now);
            ytVideo.setChannel(channel);

            YTVideo saved = videoRepository.save(ytVideo);

            VideoSnapshot snapshot = new VideoSnapshot(saved, now, saved.getViews(), saved.getLikes(), saved.getComments());
            videoSnapshotRepository.save(snapshot);
            monitoringAlertService.analyzeLatestSnapshot(saved);

            history = new ScanHistory(channel, now, ScanStatus.SUCCESS, "Single video scan OK", 1);
            scanHistoryRepository.save(history);

        } catch (IllegalArgumentException e) {
            if (history == null) {
                history = new ScanHistory(null, now, ScanStatus.FAILED, e.getMessage(), 0);
                scanHistoryRepository.save(history);
            }
            throw e;
        } catch (Exception e) {
            log.error("Błąd podczas fetchAndSaveSingleVideo dla channelId={}, videoId={}: {}", channelIdRaw, videoIdRaw, e.getMessage(), e);
            if (history == null) {
                history = new ScanHistory(null, now, ScanStatus.FAILED, e.getMessage(), 0);
                scanHistoryRepository.save(history);
            }
            throw new IllegalStateException("Błąd podczas skanowania filmu: " + e.getMessage(), e);
        }
    }

    private String resolveGoogleChannelId(String channelIdRaw) {
        if (channelIdRaw == null) {
            return null;
        }

        String value = channelIdRaw.trim();
        if (value.isEmpty()) {
            return value;
        }

        // Accept only direct YouTube channel ID value (UC...)
        if (value.startsWith("UC")) {
            return value;
        }

        return value;
    }

    private boolean isSameDay(long firstTimestamp, long secondTimestamp) {
        return java.time.Instant.ofEpochMilli(firstTimestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .equals(java.time.Instant.ofEpochMilli(secondTimestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate());
    }

}