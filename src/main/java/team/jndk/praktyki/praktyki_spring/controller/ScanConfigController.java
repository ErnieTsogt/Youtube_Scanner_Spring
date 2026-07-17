package team.jndk.praktyki.praktyki_spring.controller;

import org.springframework.web.bind.annotation.*;
import team.jndk.praktyki.praktyki_spring.model.data.AutoScanFrequency;
import team.jndk.praktyki.praktyki_spring.service.AutoScanSchedulerService;
import team.jndk.praktyki.praktyki_spring.service.DaoService;
import team.jndk.praktyki.praktyki_spring.service.VideoAutoScanSchedulerService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/scan-config")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ScanConfigController {

    private final AutoScanSchedulerService autoScanSchedulerService;
    private final VideoAutoScanSchedulerService videoAutoScanSchedulerService;
    private final DaoService daoService;

    public ScanConfigController(
            AutoScanSchedulerService autoScanSchedulerService,
            VideoAutoScanSchedulerService videoAutoScanSchedulerService,
            DaoService daoService) {
        this.autoScanSchedulerService = autoScanSchedulerService;
        this.videoAutoScanSchedulerService = videoAutoScanSchedulerService;
        this.daoService = daoService;
    }

    @GetMapping
    public Map<String, Object> getConfig() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("frequency", autoScanSchedulerService.getFrequency().name());
        response.put("intervalDays", autoScanSchedulerService.getIntervalDays());
        response.put("time", autoScanSchedulerService.getScanTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        response.put("cron", autoScanSchedulerService.getCronExpression());
        response.put("lastAutoScanDate", autoScanSchedulerService.getLastScanDate() != null
                ? autoScanSchedulerService.getLastScanDate().toString()
                : null);
        response.put("nextAutoScanAt", autoScanSchedulerService.getNextPlannedRun()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        
        // Get the last scan date of any channels (regardless of automatic/manual)
        Long lastScanMillis = daoService.getLastScanDate();
        String lastScanDateFormatted = null;
        if (lastScanMillis != null) {
            LocalDateTime lastScan = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(lastScanMillis),
                    autoScanSchedulerService.getSchedulerZone()
            );
            lastScanDateFormatted = lastScan.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        response.put("lastScanDate", lastScanDateFormatted);
        
        return response;
    }

    @PutMapping
    public Map<String, Object> updateConfig(
            @RequestParam(required = false) Integer intervalDays,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) String frequency) {
        if (frequency != null && !frequency.isBlank()) {
            AutoScanFrequency parsedFrequency = AutoScanFrequency.valueOf(frequency.toUpperCase());
            if (parsedFrequency == AutoScanFrequency.DAILY_CUSTOM) {
                int normalizedInterval = intervalDays != null ? intervalDays : autoScanSchedulerService.getIntervalDays();
                java.time.LocalTime parsedTime = time != null && !time.isBlank()
                        ? java.time.LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
                        : autoScanSchedulerService.getScanTime();
                autoScanSchedulerService.updateSchedule(normalizedInterval, parsedTime);
            } else {
                autoScanSchedulerService.updateSchedule(parsedFrequency);
            }
        } else {
            int normalizedInterval = intervalDays != null ? intervalDays : autoScanSchedulerService.getIntervalDays();
            java.time.LocalTime parsedTime = time != null && !time.isBlank()
                    ? java.time.LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
                    : autoScanSchedulerService.getScanTime();
            autoScanSchedulerService.updateSchedule(normalizedInterval, parsedTime);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("frequency", autoScanSchedulerService.getFrequency().name());
        response.put("intervalDays", autoScanSchedulerService.getIntervalDays());
        response.put("time", autoScanSchedulerService.getScanTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        response.put("cron", autoScanSchedulerService.getCronExpression());
        response.put("nextAutoScanAt", autoScanSchedulerService.getNextPlannedRun()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        response.put("message", "Auto scan schedule updated");
        return response;
    }

        @GetMapping("/video")
        public Map<String, Object> getVideoConfig() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("frequency", videoAutoScanSchedulerService.getFrequency().name());
        response.put("intervalDays", videoAutoScanSchedulerService.getIntervalDays());
        response.put("time", videoAutoScanSchedulerService.getScanTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        response.put("cron", videoAutoScanSchedulerService.getCronExpression());
        response.put("lastAutoScanDate", videoAutoScanSchedulerService.getLastScanDate() != null
            ? videoAutoScanSchedulerService.getLastScanDate().toString()
            : null);
        response.put("nextAutoScanAt", videoAutoScanSchedulerService.getNextPlannedRun()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        response.put("targetChannelId", videoAutoScanSchedulerService.getTargetChannelId());
        response.put("targetVideoId", videoAutoScanSchedulerService.getTargetVideoId());
        response.put("enabled", videoAutoScanSchedulerService.hasTarget());
        return response;
        }

        @PutMapping("/video")
        public Map<String, Object> updateVideoConfig(
            @RequestParam String channelId,
            @RequestParam String videoId,
            @RequestParam(required = false) Integer intervalDays,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) String frequency) {
        AutoScanFrequency parsedFrequency = frequency != null && !frequency.isBlank()
            ? AutoScanFrequency.valueOf(frequency.toUpperCase())
            : videoAutoScanSchedulerService.getFrequency();

        Integer normalizedInterval = parsedFrequency == AutoScanFrequency.DAILY_CUSTOM
            ? (intervalDays != null ? intervalDays : videoAutoScanSchedulerService.getIntervalDays())
            : null;

        java.time.LocalTime parsedTime = parsedFrequency == AutoScanFrequency.DAILY_CUSTOM
            ? (time != null && !time.isBlank()
            ? java.time.LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
            : videoAutoScanSchedulerService.getScanTime())
            : null;

        videoAutoScanSchedulerService.updateTargetAndSchedule(
            channelId,
            videoId,
            parsedFrequency,
            normalizedInterval,
            parsedTime
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("frequency", videoAutoScanSchedulerService.getFrequency().name());
        response.put("intervalDays", videoAutoScanSchedulerService.getIntervalDays());
        response.put("time", videoAutoScanSchedulerService.getScanTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        response.put("cron", videoAutoScanSchedulerService.getCronExpression());
        response.put("nextAutoScanAt", videoAutoScanSchedulerService.getNextPlannedRun()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        response.put("targetChannelId", videoAutoScanSchedulerService.getTargetChannelId());
        response.put("targetVideoId", videoAutoScanSchedulerService.getTargetVideoId());
        response.put("enabled", videoAutoScanSchedulerService.hasTarget());
        response.put("message", "Video auto scan schedule updated");
        return response;
        }
}
