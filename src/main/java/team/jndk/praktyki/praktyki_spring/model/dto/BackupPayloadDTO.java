package team.jndk.praktyki.praktyki_spring.model.dto;

import java.util.ArrayList;
import java.util.List;

public class BackupPayloadDTO {
    public String createdAt;
    public String timezone;
    public List<ChannelBackupDTO> channels = new ArrayList<>();
    public List<VideoBackupDTO> videos = new ArrayList<>();
    public List<VideoSnapshotBackupDTO> videoSnapshots = new ArrayList<>();
    public List<ScanHistoryBackupDTO> scanHistory = new ArrayList<>();

    public static class ChannelBackupDTO {
        public String channelName;
        public String googleId;
        public long subscribers;
    }

    public static class VideoBackupDTO {
        public String title;
        public String googleId;
        public int likes;
        public int comments;
        public int views;
        public long scannedDate;
        public String channelGoogleId;
    }

    public static class VideoSnapshotBackupDTO {
        public String videoGoogleId;
        public long snapshotDate;
        public int views;
        public int likes;
        public int comments;
    }

    public static class ScanHistoryBackupDTO {
        public String channelGoogleId;
        public long scanDate;
        public String status;
        public String message;
        public int videosSaved;
    }
}
