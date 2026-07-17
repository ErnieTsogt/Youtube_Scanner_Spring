-- Add subscriber count column to channels table
ALTER TABLE ytScanDB.channels ADD COLUMN subscribers BIGINT NOT NULL DEFAULT 0;
