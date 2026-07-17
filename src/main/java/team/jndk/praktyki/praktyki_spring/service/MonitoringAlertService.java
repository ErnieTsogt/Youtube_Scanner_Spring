package team.jndk.praktyki.praktyki_spring.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.jndk.praktyki.praktyki_spring.model.data.*;
import team.jndk.praktyki.praktyki_spring.model.dto.MonitoringAlertConfigDTO;
import team.jndk.praktyki.praktyki_spring.model.dto.MonitoringAlertDTO;
import team.jndk.praktyki.praktyki_spring.model.mapper.DtoMapper;
import team.jndk.praktyki.praktyki_spring.repository.MonitoringAlertRepository;
import team.jndk.praktyki.praktyki_spring.repository.VideoSnapshotRepository;

import java.time.Duration;
import java.util.List;

@Service
public class MonitoringAlertService {

    private volatile double popularitySpikePercent = 30.0;
    private volatile int popularitySpikeMinDelta = 100;
    private volatile double engagementDropFactor = 0.7;
    private volatile int noGrowthConsecutiveScans = 3;
    private volatile long deduplicationWindowMs = Duration.ofHours(24).toMillis();

    private final MonitoringAlertRepository monitoringAlertRepository;
    private final VideoSnapshotRepository videoSnapshotRepository;

    public MonitoringAlertService(MonitoringAlertRepository monitoringAlertRepository, VideoSnapshotRepository videoSnapshotRepository) {
        this.monitoringAlertRepository = monitoringAlertRepository;
        this.videoSnapshotRepository = videoSnapshotRepository;
    }

    @Transactional(readOnly = true)
    public MonitoringAlertConfigDTO getConfig() {
        MonitoringAlertConfigDTO dto = new MonitoringAlertConfigDTO();
        dto.popularitySpikePercent = popularitySpikePercent;
        dto.popularitySpikeMinDelta = popularitySpikeMinDelta;
        dto.engagementDropFactor = engagementDropFactor;
        dto.noGrowthConsecutiveScans = noGrowthConsecutiveScans;
        dto.deduplicationHours = (int) Duration.ofMillis(deduplicationWindowMs).toHours();
        return dto;
    }

    @Transactional
    public synchronized MonitoringAlertConfigDTO updateConfig(MonitoringAlertConfigDTO input) {
        if (input == null) {
            return getConfig();
        }

        if (input.popularitySpikePercent != null) {
            popularitySpikePercent = clampDouble(input.popularitySpikePercent, 5.0, 300.0);
        }
        if (input.popularitySpikeMinDelta != null) {
            popularitySpikeMinDelta = clampInt(input.popularitySpikeMinDelta, 1, 1_000_000);
        }
        if (input.engagementDropFactor != null) {
            engagementDropFactor = clampDouble(input.engagementDropFactor, 0.1, 1.0);
        }
        if (input.noGrowthConsecutiveScans != null) {
            noGrowthConsecutiveScans = clampInt(input.noGrowthConsecutiveScans, 3, 20);
        }
        if (input.deduplicationHours != null) {
            int normalizedHours = clampInt(input.deduplicationHours, 1, 168);
            deduplicationWindowMs = Duration.ofHours(normalizedHours).toMillis();
        }

        return getConfig();
    }

    @Transactional
    public void analyzeLatestSnapshot(YTVideo video) {
        if (video == null || video.getId() == null || video.getGoogleId() == null) {
            return;
        }

        List<VideoSnapshot> recentSnapshots = videoSnapshotRepository.findTop4ByVideo_GoogleIdOrderBySnapshotDateDesc(video.getGoogleId());
        if (recentSnapshots.size() < 2) {
            return;
        }

        VideoSnapshot current = recentSnapshots.get(0);
        VideoSnapshot previous = recentSnapshots.get(1);

        detectPopularitySpike(video, current, previous);
        detectEngagementDrop(video, current, previous);
        detectNoGrowth(video, recentSnapshots);
    }

    @Transactional(readOnly = true)
    public List<MonitoringAlertDTO> getRecentAlerts(String channelGoogleId, Boolean acknowledged, int limit) {
        int normalizedLimit = Math.max(1, Math.min(limit, 50));
        return monitoringAlertRepository.findRecentAlerts(channelGoogleId, acknowledged, PageRequest.of(0, normalizedLimit))
                .stream()
                .map(DtoMapper::toMonitoringAlertDTO)
                .toList();
    }

    @Transactional
    public MonitoringAlertDTO acknowledgeAlert(Long alertId) {
        MonitoringAlert alert = monitoringAlertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));
        alert.setAcknowledged(true);
        return DtoMapper.toMonitoringAlertDTO(monitoringAlertRepository.save(alert));
    }

    private void detectPopularitySpike(YTVideo video, VideoSnapshot current, VideoSnapshot previous) {
        if (previous.getViews() <= 0) {
            return;
        }

        int deltaViews = current.getViews() - previous.getViews();
        double growthPercent = deltaViews * 100.0 / previous.getViews();
        if (deltaViews < popularitySpikeMinDelta || growthPercent < popularitySpikePercent) {
            return;
        }

        AlertSeverity severity = growthPercent >= 100.0 ? AlertSeverity.CRITICAL : AlertSeverity.WARNING;
        String message = String.format(
                "Film \"%s\" zyskał %d wyświetleń (+%.1f%%) od poprzedniego skanu.",
                video.getTitle(),
                deltaViews,
                growthPercent
        );

        saveAlertIfNeeded(video, AlertType.POPULARITY_SPIKE, severity, message, current.getSnapshotDate());
    }

    private void detectEngagementDrop(YTVideo video, VideoSnapshot current, VideoSnapshot previous) {
        if (current.getViews() <= 0 || previous.getViews() <= 0) {
            return;
        }

        double currentEngagement = (current.getLikes() + current.getComments()) / (double) current.getViews();
        double previousEngagement = (previous.getLikes() + previous.getComments()) / (double) previous.getViews();

        if (previousEngagement <= 0 || currentEngagement >= previousEngagement * engagementDropFactor) {
            return;
        }

        String message = String.format(
                "Zaangażowanie filmu \"%s\" spadło z %.2f%% do %.2f%%.",
                video.getTitle(),
                previousEngagement * 100,
                currentEngagement * 100
        );

        saveAlertIfNeeded(video, AlertType.ENGAGEMENT_DROP, AlertSeverity.WARNING, message, current.getSnapshotDate());
    }

    private void detectNoGrowth(YTVideo video, List<VideoSnapshot> recentSnapshots) {
        if (recentSnapshots.size() < noGrowthConsecutiveScans) {
            return;
        }

        VideoSnapshot current = recentSnapshots.get(0);

        if (current.getViews() <= 0) {
            return;
        }

        boolean noGrowth = true;
        int baselineViews = current.getViews();
        for (int i = 1; i < noGrowthConsecutiveScans; i++) {
            if (recentSnapshots.get(i).getViews() != baselineViews) {
                noGrowth = false;
                break;
            }
        }

        if (noGrowth) {
            String message = String.format(
                    "Film \"%s\" nie zwiększył liczby wyświetleń przez %d kolejnych skanów.",
                    video.getTitle(),
                    noGrowthConsecutiveScans
            );
            saveAlertIfNeeded(video, AlertType.NO_GROWTH, AlertSeverity.INFO, message, current.getSnapshotDate());
        }
    }

    private void saveAlertIfNeeded(YTVideo video, AlertType alertType, AlertSeverity severity, String message, long detectedAt) {
        long deduplicationStart = detectedAt - deduplicationWindowMs;
        if (monitoringAlertRepository.existsByVideo_IdAndAlertTypeAndDetectedAtGreaterThanEqual(video.getId(), alertType, deduplicationStart)) {
            return;
        }

        MonitoringAlert alert = new MonitoringAlert(video, video.getChannel(), alertType, severity, message, detectedAt);
        monitoringAlertRepository.save(alert);
    }

    private int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private double clampDouble(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}