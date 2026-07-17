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
public class AutoScanSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(AutoScanSchedulerService.class);
    private static final ZoneId SCHEDULER_ZONE = ZoneId.of("Europe/Warsaw");

    private final TaskScheduler taskScheduler;
    private final ScanSchedulerService scanSchedulerService;

    private volatile AutoScanFrequency frequency = AutoScanFrequency.DAILY_CUSTOM;
    private volatile int intervalDays = 1;
    private volatile LocalTime scanTime = LocalTime.of(2, 0);
    private volatile LocalDate lastScanDate;
    private volatile ScheduledFuture<?> scheduledTask;

    public AutoScanSchedulerService(TaskScheduler taskScheduler, ScanSchedulerService scanSchedulerService) {
        this.taskScheduler = taskScheduler;
        this.scanSchedulerService = scanSchedulerService;
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

    public synchronized void updateSchedule(int newIntervalDays, LocalTime newScanTime) {
        int normalizedInterval = Math.max(1, newIntervalDays);
        LocalTime normalizedTime = newScanTime != null ? newScanTime : LocalTime.of(2, 0);
        reschedule(AutoScanFrequency.DAILY_CUSTOM, normalizedInterval, normalizedTime);
    }

    public synchronized void updateSchedule(AutoScanFrequency newFrequency) {
        AutoScanFrequency normalizedFrequency = newFrequency != null ? newFrequency : AutoScanFrequency.DAILY_CUSTOM;
        reschedule(normalizedFrequency, intervalDays, scanTime);
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
            log.info("Auto scan schedule updated: every {} day(s) at {} (cron: {})",
                    intervalDays, scanTime, cronExpression);
            return;
        }

        log.info("Auto scan schedule updated: {} (cron: {})", frequency, cronExpression);
    }

    private void runNow() {
        scanSchedulerService.scheduledScan();
    }

    private synchronized void runIfDue() {
        LocalDate today = LocalDate.now(SCHEDULER_ZONE);
        if (lastScanDate != null) {
            long daysSinceLast = ChronoUnit.DAYS.between(lastScanDate, today);
            if (daysSinceLast < intervalDays) {
                log.info("Auto scan skipped today ({} day(s) since last run, required: {})", daysSinceLast, intervalDays);
                return;
            }
        }

        scanSchedulerService.scheduledScan();
        lastScanDate = today;
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

    public ZoneId getSchedulerZone() {
        return SCHEDULER_ZONE;
    }
}
