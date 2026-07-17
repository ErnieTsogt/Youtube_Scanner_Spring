-- Migration: convert scanned_date to BIGINT and add indices/uniques
-- If using MySQL, modify column type to BIGINT to store epoch milliseconds

ALTER TABLE ytvideos
    MODIFY COLUMN scanned_date BIGINT;

-- Add indices using ALTER TABLE syntax with key length for TEXT columns
ALTER TABLE ytvideos ADD INDEX idx_videos_google_vid_id (google_vid_id(255));

-- Add unique constraint on channels table with key length
ALTER TABLE channels
    ADD UNIQUE KEY uq_channels_google_chan_id (google_chan_id(255));

-- Add index on channels channel_names for search with key length
ALTER TABLE channels ADD INDEX idx_channels_channel_names (channel_names(255));

