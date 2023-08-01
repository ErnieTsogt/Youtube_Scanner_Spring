create table if not exists 'Channels'
(
    ID           INTEGER
        primary key autoincrement,
    ChannelNames text,
    GoogleChanID integer
);

create table if not exists 'Videos'
(
    title       text,
    ID          integer primary key autoincrement,
    views       int,
    likes       int,
    comments    int,
    scannedDate datetime,
    ChanID      INTEGER
    constraint Videos_Channels_ID_fk
    references Channels,
    GoogleVidID integer
);