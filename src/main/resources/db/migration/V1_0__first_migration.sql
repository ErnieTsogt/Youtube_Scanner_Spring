create table if not exists videos
(
    id          integer primary key autoincrement,
    title       text,
    views       int,
    likes       int,
    comments    int,
    scanned_date datetime,
    chan_id      INTEGER
        constraint videos_channels_id_fk
            references channels,
    google_vid_id text
);

create table if not exists channels
(
    id           INTEGER primary key autoincrement,
    channel_names text,
    google_chan_id text unique
);