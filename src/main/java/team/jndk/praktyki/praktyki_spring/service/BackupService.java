package team.jndk.praktyki.praktyki_spring.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.jndk.praktyki.praktyki_spring.model.data.Channel;
import team.jndk.praktyki.praktyki_spring.model.data.ScanHistory;
import team.jndk.praktyki.praktyki_spring.model.data.ScanStatus;
import team.jndk.praktyki.praktyki_spring.model.data.VideoSnapshot;
import team.jndk.praktyki.praktyki_spring.model.data.YTVideo;
import team.jndk.praktyki.praktyki_spring.model.dto.BackupPayloadDTO;
import team.jndk.praktyki.praktyki_spring.repository.ChannelRepository;
import team.jndk.praktyki.praktyki_spring.repository.ScanHistoryRepository;
import team.jndk.praktyki.praktyki_spring.repository.VideoRepository;
import team.jndk.praktyki.praktyki_spring.repository.VideoSnapshotRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class BackupService {

    private static final ZoneId BACKUP_ZONE = ZoneId.of("Europe/Warsaw");

    private final ChannelRepository channelRepository;
    private final VideoRepository videoRepository;
    private final VideoSnapshotRepository videoSnapshotRepository;
    private final ScanHistoryRepository scanHistoryRepository;

    public BackupService(
            ChannelRepository channelRepository,
            VideoRepository videoRepository,
            VideoSnapshotRepository videoSnapshotRepository,
            ScanHistoryRepository scanHistoryRepository
    ) {
        this.channelRepository = channelRepository;
        this.videoRepository = videoRepository;
        this.videoSnapshotRepository = videoSnapshotRepository;
        this.scanHistoryRepository = scanHistoryRepository;
    }

    public BackupPayloadDTO exportBackup() {
        BackupPayloadDTO payload = new BackupPayloadDTO();
        payload.createdAt = LocalDateTime.now(BACKUP_ZONE).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        payload.timezone = BACKUP_ZONE.toString();

        List<Channel> channels = channelRepository.findAll();
        for (Channel channel : channels) {
            BackupPayloadDTO.ChannelBackupDTO dto = new BackupPayloadDTO.ChannelBackupDTO();
            dto.channelName = channel.getChannelName();
            dto.googleId = channel.getGoogleId();
            dto.subscribers = channel.getSubscribers();
            payload.channels.add(dto);
        }

        List<YTVideo> videos = videoRepository.findAll();
        for (YTVideo video : videos) {
            BackupPayloadDTO.VideoBackupDTO dto = new BackupPayloadDTO.VideoBackupDTO();
            dto.title = video.getTitle();
            dto.googleId = video.getGoogleId();
            dto.likes = video.getLikes();
            dto.comments = video.getComments();
            dto.views = video.getViews();
            dto.scannedDate = video.getScannedDate();
            dto.channelGoogleId = video.getChannel() != null ? video.getChannel().getGoogleId() : null;
            payload.videos.add(dto);
        }

        List<VideoSnapshot> snapshots = videoSnapshotRepository.findAll();
        for (VideoSnapshot snapshot : snapshots) {
            BackupPayloadDTO.VideoSnapshotBackupDTO dto = new BackupPayloadDTO.VideoSnapshotBackupDTO();
            dto.videoGoogleId = snapshot.getVideo() != null ? snapshot.getVideo().getGoogleId() : null;
            dto.snapshotDate = snapshot.getSnapshotDate();
            dto.views = snapshot.getViews();
            dto.likes = snapshot.getLikes();
            dto.comments = snapshot.getComments();
            payload.videoSnapshots.add(dto);
        }

        List<ScanHistory> historyList = scanHistoryRepository.findAll();
        for (ScanHistory history : historyList) {
            BackupPayloadDTO.ScanHistoryBackupDTO dto = new BackupPayloadDTO.ScanHistoryBackupDTO();
            dto.channelGoogleId = history.getChannel() != null ? history.getChannel().getGoogleId() : null;
            dto.scanDate = history.getScanDate();
            dto.status = history.getStatus() != null ? history.getStatus().name() : null;
            dto.message = history.getMessage();
            dto.videosSaved = history.getVideosSaved();
            payload.scanHistory.add(dto);
        }

        return payload;
    }

    @Transactional
    public Map<String, Object> restoreBackup(BackupPayloadDTO payload) {
        List<BackupPayloadDTO.ChannelBackupDTO> channelsInput = payload.channels != null ? payload.channels : new ArrayList<>();
        List<BackupPayloadDTO.VideoBackupDTO> videosInput = payload.videos != null ? payload.videos : new ArrayList<>();
        List<BackupPayloadDTO.VideoSnapshotBackupDTO> snapshotsInput = payload.videoSnapshots != null ? payload.videoSnapshots : new ArrayList<>();
        List<BackupPayloadDTO.ScanHistoryBackupDTO> historyInput = payload.scanHistory != null ? payload.scanHistory : new ArrayList<>();

        Map<String, Channel> channelByGoogleId = new HashMap<>();
        for (Channel c : channelRepository.findAll()) {
            if (c.getGoogleId() != null) {
                channelByGoogleId.put(c.getGoogleId(), c);
            }
        }

        int channelsUpserted = 0;
        for (BackupPayloadDTO.ChannelBackupDTO dto : channelsInput) {
            if (dto.googleId == null || dto.googleId.isBlank()) continue;

            Channel channel = channelByGoogleId.get(dto.googleId);
            if (channel == null) {
                channel = new Channel(dto.channelName, dto.googleId, dto.subscribers);
            } else {
                channel.setChannelName(dto.channelName);
                channel.setSubscribers(dto.subscribers);
            }

            Channel saved = channelRepository.save(channel);
            channelByGoogleId.put(saved.getGoogleId(), saved);
            channelsUpserted++;
        }

        Map<String, YTVideo> videoByGoogleId = new HashMap<>();
        for (YTVideo v : videoRepository.findAll()) {
            if (v.getGoogleId() != null) {
                videoByGoogleId.put(v.getGoogleId(), v);
            }
        }

        int videosUpserted = 0;
        int skippedVideos = 0;
        for (BackupPayloadDTO.VideoBackupDTO dto : videosInput) {
            if (dto.googleId == null || dto.googleId.isBlank()) {
                skippedVideos++;
                continue;
            }

            Channel channel = dto.channelGoogleId != null ? channelByGoogleId.get(dto.channelGoogleId) : null;
            if (channel == null) {
                skippedVideos++;
                continue;
            }

            YTVideo video = videoByGoogleId.get(dto.googleId);
            if (video == null) {
                video = new YTVideo();
            }
            video.setTitle(dto.title != null ? dto.title : "");
            video.setGoogleId(dto.googleId);
            video.setLikes(dto.likes);
            video.setComments(dto.comments);
            video.setViews(dto.views);
            video.setScannedDate(dto.scannedDate);
            video.setChannel(channel);

            YTVideo saved = videoRepository.save(video);
            videoByGoogleId.put(saved.getGoogleId(), saved);
            videosUpserted++;
        }

        int skippedSnapshots = 0;
        int snapshotsUpserted = 0;
        for (BackupPayloadDTO.VideoSnapshotBackupDTO dto : snapshotsInput) {
            if (dto.videoGoogleId == null || dto.videoGoogleId.isBlank()) {
                skippedSnapshots++;
                continue;
            }

            YTVideo video = videoByGoogleId.get(dto.videoGoogleId);
            if (video == null) {
                skippedSnapshots++;
                continue;
            }

            VideoSnapshot snapshot = videoSnapshotRepository
                    .findByVideo_GoogleIdAndSnapshotDate(dto.videoGoogleId, dto.snapshotDate)
                    .orElse(new VideoSnapshot(video, dto.snapshotDate, dto.views, dto.likes, dto.comments));

            snapshot.setVideo(video);
            snapshot.setSnapshotDate(dto.snapshotDate);
            snapshot.setViews(dto.views);
            snapshot.setLikes(dto.likes);
            snapshot.setComments(dto.comments);
            videoSnapshotRepository.save(snapshot);
            snapshotsUpserted++;
        }

        int skippedHistory = 0;
        int historyUpserted = 0;
        for (BackupPayloadDTO.ScanHistoryBackupDTO dto : historyInput) {
            if (dto.channelGoogleId == null || dto.channelGoogleId.isBlank()) {
                skippedHistory++;
                continue;
            }

            Channel channel = channelByGoogleId.get(dto.channelGoogleId);
            if (channel == null) {
                skippedHistory++;
                continue;
            }

            ScanStatus status = toScanStatus(dto.status);

            ScanHistory history = scanHistoryRepository
                    .findByChannel_GoogleIdAndScanDate(dto.channelGoogleId, dto.scanDate)
                    .orElse(new ScanHistory(channel, dto.scanDate, status, dto.message, dto.videosSaved));

            history.setChannel(channel);
            history.setScanDate(dto.scanDate);
            history.setStatus(status);
            history.setMessage(dto.message);
            history.setVideosSaved(dto.videosSaved);

            scanHistoryRepository.save(history);
            historyUpserted++;
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("channelsRestored", channelsUpserted);
        summary.put("videosRestored", videosUpserted);
        summary.put("videoSnapshotsRestored", snapshotsUpserted);
        summary.put("scanHistoryRestored", historyUpserted);
        summary.put("videosSkipped", skippedVideos);
        summary.put("videoSnapshotsSkipped", skippedSnapshots);
        summary.put("scanHistorySkipped", skippedHistory);
        summary.put("mode", "merge-upsert");

        return summary;
    }

    private ScanStatus toScanStatus(String value) {
        if (value == null || value.isBlank()) {
            return ScanStatus.PARTIAL;
        }
        try {
            return ScanStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return ScanStatus.PARTIAL;
        }
    }
}
