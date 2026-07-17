package team.jndk.praktyki.praktyki_spring.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import team.jndk.praktyki.praktyki_spring.model.data.AutoScanFrequency;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;

@Service
public class VideoAutoScanSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(VideoAutoScanSchedulerService.class);
    private static final ZoneId SCHEDULER_ZONE = ZoneId.of("Europe/Warsaw");

    private final TaskScheduler taskScheduler;
    private final ytService ytService;

    private volatile AutoScanFrequency frequency = AutoScanFrequency.DAILY_CUSTOM;
    private volatile int intervalDays = 1;
    private volatile LocalTime scanTime = LocalTime.of(2, 0);
    private volatile LocalDate lastScanDate;

    private volatile String targetChannelId;
    private volatile String targetVideoId;

    private volatile ScheduledFuture<?> scheduledTask;

    public VideoAutoScanSchedulerService(TaskScheduler taskScheduler, ytService ytService) {
        this.taskScheduler = taskScheduler;
        this.ytService = ytService;
    }

    @PostConstruct
    public void init() {
        reschedule(frequency, intervalDays, scanTime);
    }

    public synchronized AutoScanFrequency getFrequency() {
        return frequency;
    }

    public synchronized int getIntervalDays() {
        return intervalDays;
    }

    public synchronized LocalTime getScanTime() {
        return scanTime;
    }

    public synchronized LocalDate getLastScanDate() {
        return lastScanDate;
    }

    public synchronized String getTargetChannelId() {
        return targetChannelId;
    }

    public synchronized String getTargetVideoId() {
        return targetVideoId;
    }

    public synchronized boolean hasTarget() {
        return targetChannelId != null && !targetChannelId.isBlank()
                && targetVideoId != null && !targetVideoId.isBlank();
    }

    public synchronized void updateTargetAndSchedule(String channelId, String videoId, AutoScanFrequency newFrequency, Integer newIntervalDays, LocalTime newScanTime) {
        this.targetChannelId = channelId;
        this.targetVideoId = videoId;

        AutoScanFrequency normalizedFrequency = newFrequency != null ? newFrequency : this.frequency;
        int normalizedIntervalDays = newIntervalDays != null ? Math.max(1, newIntervalDays) : this.intervalDays;
        LocalTime normalizedTime = newScanTime != null ? newScanTime : this.scanTime;

        if (normalizedFrequency == AutoScanFrequency.DAILY_CUSTOM) {
            reschedule(normalizedFrequency, normalizedIntervalDays, normalizedTime);
        } else {
            reschedule(normalizedFrequency, this.intervalDays, this.scanTime);
        }
    }

    private void reschedule(AutoScanFrequency newFrequency, int newIntervalDays, LocalTime newScanTime) {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTask = null;
        }

        this.frequency = newFrequency;
        this.intervalDays = newIntervalDays;
        this.scanTime = newScanTime;

        String cronExpression = buildCronExpression();
        Runnable task = frequency == AutoScanFrequency.DAILY_CUSTOM ? this::runIfDue : this::runNow;

        scheduledTask = taskScheduler.schedule(
                task,
                new CronTrigger(cronExpression, TimeZone.getTimeZone(SCHEDULER_ZONE))
        );

        if (frequency == AutoScanFrequency.DAILY_CUSTOM) {
            log.info("Video auto scan schedule updated: every {} day(s) at {} (cron: {}), targetChannel={}, targetVideo={}",
                    intervalDays, scanTime, cronExpression, targetChannelId, targetVideoId);
            return;
        }

        log.info("Video auto scan schedule updated: {} (cron: {}), targetChannel={}, targetVideo={}",
                frequency, cronExpression, targetChannelId, targetVideoId);
    }

    private void runNow() {
        scanVideoIfTargetExists();
    }

    private synchronized void runIfDue() {
        if (!hasTarget()) {
            return;
        }

        LocalDate today = LocalDate.now(SCHEDULER_ZONE);
        if (lastScanDate != null) {
            long daysSinceLast = ChronoUnit.DAYS.between(lastScanDate, today);
            if (daysSinceLast < intervalDays) {
                log.info("Video auto scan skipped today ({} day(s) since last run, required: {})", daysSinceLast, intervalDays);
                return;
            }
        }

        if (scanVideoIfTargetExists()) {
            lastScanDate = today;
        }
    }

    private boolean scanVideoIfTargetExists() {
        String channelIdSnapshot = targetChannelId;
        String videoIdSnapshot = targetVideoId;

        if (channelIdSnapshot == null || channelIdSnapshot.isBlank() || videoIdSnapshot == null || videoIdSnapshot.isBlank()) {
            log.info("Video auto scan skipped - target not configured");
            return false;
        }

        try {
            ytService.fetchAndSaveSingleVideo(channelIdSnapshot, videoIdSnapshot);
            if (frequency != AutoScanFrequency.DAILY_CUSTOM) {
                synchronized (this) {
                    lastScanDate = LocalDate.now(SCHEDULER_ZONE);
                }
            }
            return true;
        } catch (Exception e) {
            log.warn("Error scanning selected video {} for channel {}: {}", videoIdSnapshot, channelIdSnapshot, e.getMessage());
            return false;
        }
    }

    public synchronized String getCronExpression() {
        return buildCronExpression();
    }

    public synchronized LocalDateTime getNextPlannedRun() {
        LocalDateTime now = LocalDateTime.now(SCHEDULER_ZONE);

        if (frequency == AutoScanFrequency.EVERY_MINUTE) {
            return now.plusMinutes(1).withSecond(0).withNano(0);
        }

        if (frequency == AutoScanFrequency.EVERY_15_MIN) {
            int nextMinute = ((now.getMinute() / 15) + 1) * 15;
            LocalDateTime candidate = now.withSecond(0).withNano(0);
            if (nextMinute >= 60) {
                return candidate.plusHours(1).withMinute(0);
            }
            return candidate.withMinute(nextMinute);
        }

        if (frequency == AutoScanFrequency.EVERY_30_MIN) {
            int nextMinute = now.getMinute() < 30 ? 30 : 60;
            LocalDateTime candidate = now.withSecond(0).withNano(0);
            if (nextMinute == 60) {
                return candidate.plusHours(1).withMinute(0);
            }
            return candidate.withMinute(nextMinute);
        }

        if (frequency == AutoScanFrequency.HOURLY) {
            return now.plusHours(1)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);
        }

        LocalDate today = LocalDate.now(SCHEDULER_ZONE);
        LocalTime nowTime = LocalTime.now(SCHEDULER_ZONE);

        LocalDate nextDate;
        if (lastScanDate == null) {
            nextDate = !scanTime.isAfter(nowTime) ? today.plusDays(1) : today;
        } else {
            LocalDate earliestAllowedDate = lastScanDate.plusDays(intervalDays);
            nextDate = earliestAllowedDate.isAfter(today) ? earliestAllowedDate : today;

            if (nextDate.equals(today) && !scanTime.isAfter(nowTime)) {
                nextDate = today.plusDays(1);
            }
        }

        return LocalDateTime.of(nextDate, scanTime);
    }

    private String buildCronExpression() {
        if (frequency == AutoScanFrequency.DAILY_CUSTOM) {
            return String.format("0 %d %d * * *", scanTime.getMinute(), scanTime.getHour());
        }

        String cron = frequency.getCronExpression();
        if (cron == null || cron.isBlank()) {
            return String.format("0 %d %d * * *", scanTime.getMinute(), scanTime.getHour());
        }
        return cron;
    }
}
