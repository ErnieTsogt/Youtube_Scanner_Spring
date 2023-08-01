create table if not exists myVideos
(
    ID          integer primary key autoincrement,
    title       text,
    views       int,
    likes       int,
    comments    int,
    scannedDate datetime,
    ChanID      INTEGER
        constraint Videos_Channels_ID_fk
            references Channels,
    GoogleVidID text
);

create table if not exists myChannels
(
    ID           INTEGER primary key autoincrement,
    ChannelNames text,
    GoogleChanID text unique
);

drop table Videos;
drop table  Channels;

ALTER TABLE myChannels RENAME TO Channels;
ALTER TABLE myVideos RENAME TO Videos;
