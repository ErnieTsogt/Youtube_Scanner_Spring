-- Create table for scan history to track automatic scans
CREATE TABLE IF NOT EXISTS scan_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chan_id INT,
    scan_date BIGINT NOT NULL,
    status VARCHAR(255) CHECK (status IN ('SUCCESS', 'FAILED', 'PARTIAL')),
    message VARCHAR(2000),
    videos_saved INT NOT NULL,
    CONSTRAINT fk_scan_history_channel FOREIGN KEY (chan_id) REFERENCES channels(id),
    INDEX idx_scan_history_channel_id (chan_id),
    INDEX idx_scan_history_scan_date (scan_date)
);

