package team.jndk.praktyki.praktyki_spring.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import team.jndk.praktyki.praktyki_spring.model.data.YTVideo;
import team.jndk.praktyki.praktyki_spring.model.data.VideoSnapshot;
import team.jndk.praktyki.praktyki_spring.repository.ChannelRepository;
import team.jndk.praktyki.praktyki_spring.repository.VideoRepository;
import team.jndk.praktyki.praktyki_spring.repository.VideoSnapshotRepository;
import team.jndk.praktyki.praktyki_spring.repository.ScanHistoryRepository;
import team.jndk.praktyki.praktyki_spring.model.data.ScanHistory;
import team.jndk.praktyki.praktyki_spring.service.youtube.YouTubeClient;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class YtServiceTest {

    private YouTubeClient youTubeClient;
    private VideoRepository videoRepository;
    private ChannelRepository channelRepository;
    private VideoSnapshotRepository videoSnapshotRepository;
    private ScanHistoryRepository scanHistoryRepository;
    private MonitoringAlertService monitoringAlertService;
    private ytService service;

    @BeforeEach
    public void setUp() {
        youTubeClient = Mockito.mock(YouTubeClient.class);
        videoRepository = Mockito.mock(VideoRepository.class);
        channelRepository = Mockito.mock(ChannelRepository.class);
        videoSnapshotRepository = Mockito.mock(VideoSnapshotRepository.class);
        scanHistoryRepository = Mockito.mock(ScanHistoryRepository.class);
        monitoringAlertService = Mockito.mock(MonitoringAlertService.class);

        service = new ytService(youTubeClient, videoRepository, channelRepository, videoSnapshotRepository, scanHistoryRepository, monitoringAlertService);
    }

    @Test
    public void fetchAndSaveVideos_savesVideoAndSnapshot() throws Exception {
        String channelId = "chan123";
        when(youTubeClient.getChannelInfo(channelId))
                .thenReturn(Optional.of(new YouTubeClient.YTApiChannel("TestChannel", 1_000_000L)));
        when(youTubeClient.listVideoIdsForChannel(channelId, 50)).thenReturn(Collections.singletonList("vid1"));
        YouTubeClient.YTApiVideo apiVideo = new YouTubeClient.YTApiVideo("vid1", "Title1", 10, 2, 100);
        when(youTubeClient.getVideoDetails("vid1")).thenReturn(apiVideo);

        // mock saving video to return the same video entity with default id 0
        when(videoRepository.save(any(YTVideo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.fetchAndSaveVideos(channelId);

        verify(videoRepository, times(1)).save(any(YTVideo.class));
        ArgumentCaptor<VideoSnapshot> captor = ArgumentCaptor.forClass(VideoSnapshot.class);
        verify(videoSnapshotRepository, times(1)).save(captor.capture());

        VideoSnapshot snap = captor.getValue();
        assertEquals(100, snap.getViews());
        assertEquals(10, snap.getLikes());
        assertEquals(2, snap.getComments());

        // verify scan history saved
        ArgumentCaptor<ScanHistory> histCaptor = ArgumentCaptor.forClass(ScanHistory.class);
        verify(scanHistoryRepository, times(1)).save(histCaptor.capture());
        ScanHistory savedHist = histCaptor.getValue();
        assertEquals(1, savedHist.getVideosSaved());
    }
}
