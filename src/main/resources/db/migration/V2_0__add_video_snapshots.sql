-- Create table for video snapshots to store historical metrics
CREATE TABLE IF NOT EXISTS ytScanDB.video_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    video_id INT,
    snapshot_date BIGINT,
    views INT,
    likes INT,
    comments INT,
    CONSTRAINT fk_snapshot_video FOREIGN KEY (video_id) REFERENCES ytScanDB.ytvideos(id),
    INDEX idx_snapshot_video_id (video_id)
);

