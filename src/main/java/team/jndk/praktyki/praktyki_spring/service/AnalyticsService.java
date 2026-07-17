package team.jndk.praktyki.praktyki_spring.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.jndk.praktyki.praktyki_spring.model.data.*;
import team.jndk.praktyki.praktyki_spring.model.dto.ChannelDTO;
import team.jndk.praktyki.praktyki_spring.model.dto.VideoDTO;
import team.jndk.praktyki.praktyki_spring.model.dto.VideoStatsHistoryDTO;
import team.jndk.praktyki.praktyki_spring.repository.ChannelRepository;
import team.jndk.praktyki.praktyki_spring.repository.MonitoringAlertRepository;
import team.jndk.praktyki.praktyki_spring.repository.ScanHistoryRepository;
import team.jndk.praktyki.praktyki_spring.repository.VideoSnapshotRepository;
import team.jndk.praktyki.praktyki_spring.repository.VideoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final ChannelRepository channelRepository;
    private final VideoRepository videoRepository;
    private final VideoSnapshotRepository videoSnapshotRepository;
    private final ScanHistoryRepository scanHistoryRepository;
    private final MonitoringAlertRepository monitoringAlertRepository;

    public AnalyticsService(ChannelRepository channelRepository, VideoRepository videoRepository, VideoSnapshotRepository videoSnapshotRepository, ScanHistoryRepository scanHistoryRepository, MonitoringAlertRepository monitoringAlertRepository) {
        this.channelRepository = channelRepository;
        this.videoRepository = videoRepository;
        this.videoSnapshotRepository = videoSnapshotRepository;
        this.scanHistoryRepository = scanHistoryRepository;
        this.monitoringAlertRepository = monitoringAlertRepository;
    }

    /**
     * Get all channels for filter dropdown
     */
    public List<ChannelDTO> getAllChannels() {
        return channelRepository.findAll().stream()
                .map(this::convertChannelToDTO)
                .sorted(Comparator.comparing(c -> c.channelName))
                .collect(Collectors.toList());
    }

    /**
     * Filter videos with pagination
     */
    public Page<VideoDTO> filterVideos(String channelTitle, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        long fromTimestamp = fromDate != null
                ? fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                : Long.MIN_VALUE;
        long toTimestamp = toDate != null
                ? toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                : Long.MAX_VALUE;

        List<YTVideo> filtered = videoRepository.findAll().stream()
                .filter(v -> channelTitle == null || channelTitle.isEmpty() ||
                        (v.getChannel() != null && v.getChannel().getGoogleId().equalsIgnoreCase(channelTitle)))
                .filter(v -> fromDate == null || v.getScannedDate() >= fromTimestamp)
                .filter(v -> toDate == null || v.getScannedDate() < toTimestamp)
                .collect(Collectors.toList());

        // Show only latest row for each YouTube video in table (prevents duplicate titles with older stats)
        filtered = getLatestVideos(filtered);

        Comparator<YTVideo> comparator = buildVideoComparator(pageable.getSort());
        if (comparator != null) {
            filtered.sort(comparator);
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<VideoDTO> pageContent = start >= filtered.size()
                ? Collections.emptyList()
                : filtered.subList(start, end).stream().map(this::convertVideoToDTO).collect(Collectors.toList());

        return new PageImpl<>(pageContent, pageable, filtered.size());
    }

    public Page<VideoStatsHistoryDTO> getVideoStatsHistory(String channelTitle, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        Long fromTimestamp = fromDate != null
                ? fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                : null;
        Long toTimestamp = toDate != null
                ? toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                : null;

        Page<VideoSnapshot> snapshots = videoSnapshotRepository.findHistory(channelTitle, fromTimestamp, toTimestamp, pageable);
        return snapshots.map(this::convertSnapshotToDTO);
    }

    private Comparator<YTVideo> buildVideoComparator(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return Comparator.comparingLong(YTVideo::getScannedDate).reversed();
        }

        Comparator<YTVideo> comparator = null;
        for (Sort.Order order : sort) {
            Comparator<YTVideo> fieldComparator;
            String property = order.getProperty();

            switch (property) {
                case "title":
                    fieldComparator = Comparator.comparing(
                            v -> v.getTitle() == null ? "" : v.getTitle(),
                            String.CASE_INSENSITIVE_ORDER
                    );
                    break;
                case "likes":
                    fieldComparator = Comparator.comparingInt(YTVideo::getLikes);
                    break;
                case "comments":
                    fieldComparator = Comparator.comparingInt(YTVideo::getComments);
                    break;
                case "scannedDate":
                    fieldComparator = Comparator.comparingLong(YTVideo::getScannedDate);
                    break;
                case "views":
                default:
                    fieldComparator = Comparator.comparingInt(YTVideo::getViews);
                    break;
            }

            if (order.getDirection() == Sort.Direction.DESC) {
                fieldComparator = fieldComparator.reversed();
            }

            comparator = comparator == null ? fieldComparator : comparator.thenComparing(fieldComparator);
        }

        return comparator;
    }

    /**
     * Get trend data for charts
     */
    public Map<String, Object> getTrends(String channelTitle, String metric, Integer days) {
        int normalizedDays = days != null ? days : 30;
        long fromTimestamp = LocalDate.now()
                .minusDays(normalizedDays)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        // Dla trendów metryk wideo używamy snapshotów i agregacji minutowej,
        // żeby wykres pokazywał zmiany nawet przy skanie co minutę.
        if (metric == null || !metric.equalsIgnoreCase("subscribers")) {
            List<VideoSnapshot> snapshots = videoSnapshotRepository.findForTrends(channelTitle, fromTimestamp);

            // Step 1: group snapshots by minute, keeping only the latest snapshot per video per minute
            Map<LocalDateTime, Map<String, VideoSnapshot>> latestSnapshotByMinuteAndVideo = new LinkedHashMap<>();
            for (VideoSnapshot snapshot : snapshots) {
                LocalDateTime minuteKey = LocalDateTime.ofInstant(
                        new Date(snapshot.getSnapshotDate()).toInstant(),
                        ZoneId.systemDefault()
                ).truncatedTo(ChronoUnit.MINUTES);

                String videoKey = snapshot.getVideo() != null && snapshot.getVideo().getGoogleId() != null
                        ? snapshot.getVideo().getGoogleId()
                        : "snapshot-" + snapshot.getId();

                Map<String, VideoSnapshot> perVideo = latestSnapshotByMinuteAndVideo
                        .computeIfAbsent(minuteKey, ignored -> new LinkedHashMap<>());

                VideoSnapshot existing = perVideo.get(videoKey);
                if (existing == null || snapshot.getSnapshotDate() > existing.getSnapshotDate()) {
                    perVideo.put(videoKey, snapshot);
                }
            }

            // Step 2: cumulative global state — carry forward latest value per video across minutes.
            // This prevents oscillation when a single scan spans more than one minute: videos whose
            // snapshots land in minute T+1 still contribute their previous (minute T) value at minute T,
            // and their new value is reflected starting from minute T+1.
            Map<String, Long> globalLatestByVideo = new LinkedHashMap<>();
            Map<LocalDateTime, Long> aggregatedByMinute = new LinkedHashMap<>();
            latestSnapshotByMinuteAndVideo.forEach((minute, perVideo) -> {
                // Update global state for every video seen in this minute
                perVideo.forEach((videoKey, snapshot) ->
                        globalLatestByVideo.put(videoKey, getSnapshotMetricValue(snapshot, metric)));

                // Sum ALL ever-seen videos (not just the ones updated this minute)
                long total = globalLatestByVideo.values().stream().mapToLong(Long::longValue).sum();
                aggregatedByMinute.put(minute, total);
            });

            List<String> dates = new ArrayList<>();
            List<Long> values = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            aggregatedByMinute.forEach((minute, value) -> {
                dates.add(minute.format(formatter));
                values.add(value);
            });

            Map<String, Object> trends = new LinkedHashMap<>();
            trends.put("dates", dates);
            trends.put("values", values);
            trends.put("trendDirection", values.isEmpty() ? "stable" :
                    (values.get(values.size() - 1) > values.get(0) ? "up" : "down"));

            return trends;
        }

        List<YTVideo> videos;
        if (channelTitle != null && !channelTitle.isEmpty()) {
            videos = videoRepository.findAll().stream()
                    .filter(v -> v.getChannel() != null && v.getChannel().getGoogleId().equalsIgnoreCase(channelTitle))
                    .collect(Collectors.toList());
        } else {
            videos = videoRepository.findAll();
        }

        videos = getLatestVideosPerDay(videos);

        LocalDate startDate = LocalDate.now().minusDays(normalizedDays);
        List<YTVideo> filteredVideos = videos.stream()
                .filter(v -> {
                    LocalDate videoDate = new Date(v.getScannedDate()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return !videoDate.isBefore(startDate);
                })
                .sorted(Comparator.comparing(YTVideo::getScannedDate))
                .collect(Collectors.toList());

        Map<String, Object> trends = new LinkedHashMap<>();
        List<String> dates = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        Map<LocalDate, Long> aggregated = new LinkedHashMap<>();
        if (metric != null && metric.equalsIgnoreCase("subscribers")) {
            Map<LocalDate, Map<Integer, Long>> subscribersByDayAndChannel = new LinkedHashMap<>();
            filteredVideos.forEach(v -> {
                LocalDate date = new Date(v.getScannedDate()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                Integer channelId = v.getChannel() != null ? v.getChannel().getId() : null;
                if (channelId == null) return;
                subscribersByDayAndChannel
                        .computeIfAbsent(date, d -> new LinkedHashMap<>())
                        .put(channelId, v.getChannel().getSubscribers());
            });

            subscribersByDayAndChannel.forEach((date, channelMap) -> {
                long dailySubscribers = channelMap.values().stream().mapToLong(Long::longValue).sum();
                aggregated.put(date, dailySubscribers);
            });
        } else {
            filteredVideos.forEach(video -> {
                LocalDate date = new Date(video.getScannedDate()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                long metricValue = getMetricValue(video, metric);
                aggregated.merge(date, metricValue, Long::sum);
            });
        }

        aggregated.forEach((date, value) -> {
            dates.add(date.toString());
            values.add(value);
        });

        trends.put("dates", dates);
        trends.put("values", values);
        trends.put("trendDirection", values.isEmpty() ? "stable" : 
                   (values.get(values.size() - 1) > values.get(0) ? "up" : "down"));

        return trends;
    }

    /**
     * Get overall statistics
     */
    public Map<String, Object> getStatistics(String channelTitle) {
        List<YTVideo> videos;

        if (channelTitle != null && !channelTitle.isEmpty()) {
            videos = videoRepository.findAll().stream()
                    .filter(v -> v.getChannel() != null && v.getChannel().getGoogleId().equalsIgnoreCase(channelTitle))
                    .collect(Collectors.toList());
        } else {
            videos = videoRepository.findAll();
        }

        // Use only latest snapshot per YouTube video ID to avoid summing historical scans
        videos = getLatestVideos(videos);

        long totalViews = videos.stream().mapToLong(YTVideo::getViews).sum();
        long totalLikes = videos.stream().mapToLong(YTVideo::getLikes).sum();
        long totalComments = videos.stream().mapToLong(YTVideo::getComments).sum();

        double avgViews = videos.isEmpty() ? 0 : (double) totalViews / videos.size();
        double avgLikes = videos.isEmpty() ? 0 : (double) totalLikes / videos.size();
        double avgComments = videos.isEmpty() ? 0 : (double) totalComments / videos.size();

        YTVideo topVideo = videos.stream()
                .max(Comparator.comparing(YTVideo::getViews))
                .orElse(null);

        // Count unique channels
        long channelCount = videos.stream()
                .map(v -> v.getChannel() != null ? v.getChannel().getId() : null)
                .filter(id -> id != null)
                .distinct()
                .count();

        // Sum subscribers from distinct channels
        long totalSubscribers = videos.stream()
                .map(YTVideo::getChannel)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Channel::getId,
                        Channel::getSubscribers,
                        (a, b) -> a
                ))
                .values().stream()
                .mapToLong(Long::longValue)
                .sum();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalVideos", videos.size());
        stats.put("totalViews", totalViews);
        stats.put("totalLikes", totalLikes);
        stats.put("totalComments", totalComments);
        stats.put("totalSubscribers", totalSubscribers);
        stats.put("avgViews", Math.round(avgViews));
        stats.put("avgLikes", Math.round(avgLikes));
        stats.put("avgComments", Math.round(avgComments));
        stats.put("topVideo", topVideo != null ? convertVideoToDTO(topVideo) : null);
        stats.put("channelCount", channelCount);

        return stats;
    }

    /**
     * Compare channels
     */
    public List<Map<String, Object>> compareChannels() {
        return compareChannels(null, "views");
    }

    public List<Map<String, Object>> compareChannels(String channelTitle) {
        return compareChannels(channelTitle, "views");
    }

    public List<Map<String, Object>> compareChannels(String channelTitle, String metric) {
        return channelRepository.findAll().stream()
                .filter(channel -> channelTitle == null || channelTitle.isEmpty() ||
                        (channel.getGoogleId() != null && channel.getGoogleId().equalsIgnoreCase(channelTitle)))
                .map(channel -> {
                    Map<String, Object> comparison = new LinkedHashMap<>();
                    List<YTVideo> channelVideos = videoRepository.findAll().stream()
                            .filter(v -> v.getChannel() != null && v.getChannel().getId().equals(channel.getId()))
                            .collect(Collectors.toList());

                    // Use only latest snapshot per video for fair channel distribution
                    channelVideos = getLatestVideos(channelVideos);

                    long totalViews = channelVideos.stream().mapToLong(YTVideo::getViews).sum();
                    long totalLikes = channelVideos.stream().mapToLong(YTVideo::getLikes).sum();
                    long totalComments = channelVideos.stream().mapToLong(YTVideo::getComments).sum();
                    long totalSubscribers = channel.getSubscribers();

                    long metricValue;
                    String normalizedMetric = metric == null ? "views" : metric.toLowerCase();
                    switch (normalizedMetric) {
                        case "likes":
                            metricValue = totalLikes;
                            break;
                        case "comments":
                            metricValue = totalComments;
                            break;
                        case "subscribers":
                            metricValue = totalSubscribers;
                            break;
                        case "views":
                        default:
                            metricValue = totalViews;
                            break;
                    }

                    comparison.put("name", channel.getChannelName());
                    comparison.put("videoCount", channelVideos.size());
                    comparison.put("totalViews", totalViews);
                    comparison.put("totalLikes", totalLikes);
                    comparison.put("totalComments", totalComments);
                    comparison.put("subscribers", channel.getSubscribers());
                    comparison.put("metric", normalizedMetric);
                    comparison.put("metricValue", metricValue);
                    comparison.put("channelId", channel.getGoogleId());

                    return comparison;
                })
                .collect(Collectors.toList());
    }

    // Helper methods

    private long getMetricValue(YTVideo video, String metric) {
        if (metric == null || metric.equalsIgnoreCase("views")) {
            return video.getViews();
        } else if (metric.equalsIgnoreCase("likes")) {
            return video.getLikes();
        } else if (metric.equalsIgnoreCase("comments")) {
            return video.getComments();
        } else if (metric.equalsIgnoreCase("subscribers")) {
            return video.getChannel() != null ? video.getChannel().getSubscribers() : 0;
        }
        return 0;
    }

    private long getSnapshotMetricValue(VideoSnapshot snapshot, String metric) {
        if (metric == null || metric.equalsIgnoreCase("views")) {
            return snapshot.getViews();
        }
        if (metric.equalsIgnoreCase("likes")) {
            return snapshot.getLikes();
        }
        if (metric.equalsIgnoreCase("comments")) {
            return snapshot.getComments();
        }
        return snapshot.getViews();
    }

    private List<YTVideo> getLatestVideos(List<YTVideo> videos) {
        Map<String, YTVideo> latestByGoogleId = new LinkedHashMap<>();

        for (YTVideo video : videos) {
            if (video == null) continue;

            String key = video.getGoogleId();
            if (key == null || key.isEmpty()) {
                key = "db-" + video.getId();
            }

            YTVideo existing = latestByGoogleId.get(key);
            if (existing == null || video.getScannedDate() > existing.getScannedDate()) {
                latestByGoogleId.put(key, video);
            }
        }

        return new ArrayList<>(latestByGoogleId.values());
    }

    private List<YTVideo> getLatestVideosPerDay(List<YTVideo> videos) {
        Map<String, YTVideo> latestByVideoAndDay = new LinkedHashMap<>();

        for (YTVideo video : videos) {
            if (video == null) continue;

            String videoKey = video.getGoogleId();
            if (videoKey == null || videoKey.isEmpty()) {
                videoKey = "db-" + video.getId();
            }

            LocalDate day = new Date(video.getScannedDate())
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            String key = videoKey + "|" + day;
            YTVideo existing = latestByVideoAndDay.get(key);
            if (existing == null || video.getScannedDate() > existing.getScannedDate()) {
                latestByVideoAndDay.put(key, video);
            }
        }

        return new ArrayList<>(latestByVideoAndDay.values());
    }


    private ChannelDTO convertChannelToDTO(Channel channel) {
        ChannelDTO dto = new ChannelDTO();
        dto.id = channel.getId();
        dto.googleId = channel.getGoogleId();
        dto.channelName = channel.getChannelName();
        dto.subscribers = channel.getSubscribers();
        return dto;
    }

    private VideoDTO convertVideoToDTO(YTVideo video) {
        VideoDTO dto = new VideoDTO();
        dto.id = video.getId();
        dto.googleId = video.getGoogleId();
        dto.title = video.getTitle();
        dto.scannedDate = video.getScannedDate();
        dto.views = video.getViews();
        dto.likes = video.getLikes();
        dto.comments = video.getComments();
        if (video.getChannel() != null) {
            dto.channelId = video.getChannel().getId();
        }
        return dto;
    }

    private VideoStatsHistoryDTO convertSnapshotToDTO(VideoSnapshot snapshot) {
        VideoStatsHistoryDTO dto = new VideoStatsHistoryDTO();
        dto.snapshotId = snapshot.getId();
        dto.snapshotDate = snapshot.getSnapshotDate();
        dto.views = snapshot.getViews();
        dto.likes = snapshot.getLikes();
        dto.comments = snapshot.getComments();

        if (snapshot.getVideo() != null) {
            dto.videoTitle = snapshot.getVideo().getTitle();
            dto.videoGoogleId = snapshot.getVideo().getGoogleId();
            if (snapshot.getVideo().getChannel() != null) {
                dto.channelGoogleId = snapshot.getVideo().getChannel().getGoogleId();
                dto.channelName = snapshot.getVideo().getChannel().getChannelName();
            }
        }

        return dto;
    }

    /**
     * Delete all videos/scans for a specific channel
     */
    @Transactional
    public int deleteChannelVideos(String googleId) {
        monitoringAlertRepository.deleteByChannelGoogleId(googleId);
        videoSnapshotRepository.deleteByChannelGoogleId(googleId);
        return videoRepository.deleteByChannelGoogleId(googleId);
    }

    /**
     * Delete channel from list/view (and its scans)
     */
    @Transactional
    public long deleteChannel(String googleId) {
        monitoringAlertRepository.deleteByChannelGoogleId(googleId);
        scanHistoryRepository.deleteByChannelGoogleId(googleId);
        videoSnapshotRepository.deleteByChannelGoogleId(googleId);
        videoRepository.deleteByChannelGoogleId(googleId);
        return channelRepository.deleteByGoogleId(googleId);
    }
}
