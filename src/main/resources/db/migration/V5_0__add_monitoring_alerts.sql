CREATE TABLE IF NOT EXISTS ytScanDB.monitoring_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    video_id INT,
    chan_id INT,
    alert_type VARCHAR(255) NOT NULL CHECK (alert_type IN ('POPULARITY_SPIKE', 'NO_GROWTH', 'ENGAGEMENT_DROP')),
    severity VARCHAR(255) NOT NULL CHECK (severity IN ('INFO', 'WARNING', 'CRITICAL')),
    message VARCHAR(2000) NOT NULL,
    detected_at BIGINT NOT NULL,
    acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_monitoring_alert_video FOREIGN KEY (video_id) REFERENCES ytScanDB.ytvideos(id),
    CONSTRAINT fk_monitoring_alert_channel FOREIGN KEY (chan_id) REFERENCES ytScanDB.channels(id),
    INDEX idx_monitoring_alert_detected_at (detected_at),
    INDEX idx_monitoring_alert_acknowledged (acknowledged),
    INDEX idx_monitoring_alert_channel (chan_id),
    INDEX idx_monitoring_alert_video (video_id)
);