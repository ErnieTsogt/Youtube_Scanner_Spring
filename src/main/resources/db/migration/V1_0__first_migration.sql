CREATE TABLE IF NOT EXISTS ytScanDB.channels (
                                        id           INT AUTO_INCREMENT PRIMARY KEY,
                                        channel_names TEXT,
                                        google_chan_id TEXT
);

CREATE TABLE IF NOT EXISTS ytScanDB.YTVideos (
                                        id          INT AUTO_INCREMENT PRIMARY KEY,
                                        title       TEXT,
                                        views       INT,
                                        likes       INT,
                                        comments    INT,
                                        scanned_date DATETIME,
                                        chan_id     INT,
                                        google_vid_id TEXT,
                                        CONSTRAINT videos_channels_id_fk FOREIGN KEY (chan_id) REFERENCES channels(id)
);