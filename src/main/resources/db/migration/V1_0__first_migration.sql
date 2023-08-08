create table if not exists Videos
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

create table if not exists Channels
(
    ID           INTEGER primary key autoincrement,
    ChannelNames text,
    GoogleChanID text unique
);