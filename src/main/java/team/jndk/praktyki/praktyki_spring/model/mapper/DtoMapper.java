package team.jndk.praktyki.praktyki_spring.model.mapper;

import team.jndk.praktyki.praktyki_spring.model.data.Channel;
import team.jndk.praktyki.praktyki_spring.model.data.MonitoringAlert;
import team.jndk.praktyki.praktyki_spring.model.data.YTVideo;
import team.jndk.praktyki.praktyki_spring.model.dto.ChannelDTO;
import team.jndk.praktyki.praktyki_spring.model.dto.MonitoringAlertDTO;
import team.jndk.praktyki.praktyki_spring.model.dto.VideoDTO;

public class DtoMapper {

    public static ChannelDTO toChannelDTO(Channel c) {
        if (c == null) return null;
        ChannelDTO d = new ChannelDTO();
        d.id = c.getId();
        d.channelName = c.getChannelName();
        d.googleId = c.getGoogleId();
        return d;
    }

    public static VideoDTO toVideoDTO(YTVideo v) {
        if (v == null) return null;
        VideoDTO d = new VideoDTO();
        d.id = v.getId();
        d.title = v.getTitle();
        d.googleId = v.getGoogleId();
        d.likes = v.getLikes();
        d.comments = v.getComments();
        d.views = v.getViews();
        d.scannedDate = v.getScannedDate();
        d.channelId = v.getChannel() != null ? v.getChannel().getId() : 0;
        return d;
    }

    public static team.jndk.praktyki.praktyki_spring.model.dto.ScanHistoryDTO toScanHistoryDTO(team.jndk.praktyki.praktyki_spring.model.data.ScanHistory h) {
        if (h == null) return null;
        team.jndk.praktyki.praktyki_spring.model.dto.ScanHistoryDTO d = new team.jndk.praktyki.praktyki_spring.model.dto.ScanHistoryDTO();
        d.id = h.getId();
        d.channelId = h.getChannel() != null ? h.getChannel().getId() : 0;
        d.scanDate = h.getScanDate();
        d.status = h.getStatus() != null ? h.getStatus().name() : null;
        d.message = h.getMessage();
        d.videosSaved = h.getVideosSaved();
        return d;
    }

    public static MonitoringAlertDTO toMonitoringAlertDTO(MonitoringAlert alert) {
        if (alert == null) return null;
        MonitoringAlertDTO d = new MonitoringAlertDTO();
        d.id = alert.getId();
        d.videoId = alert.getVideo() != null ? alert.getVideo().getId() : null;
        d.videoTitle = alert.getVideo() != null ? alert.getVideo().getTitle() : null;
        d.videoGoogleId = alert.getVideo() != null ? alert.getVideo().getGoogleId() : null;
        d.channelName = alert.getChannel() != null ? alert.getChannel().getChannelName() : null;
        d.channelGoogleId = alert.getChannel() != null ? alert.getChannel().getGoogleId() : null;
        d.alertType = alert.getAlertType() != null ? alert.getAlertType().name() : null;
        d.severity = alert.getSeverity() != null ? alert.getSeverity().name() : null;
        d.message = alert.getMessage();
        d.detectedAt = alert.getDetectedAt();
        d.acknowledged = alert.isAcknowledged();
        return d;
    }
}
