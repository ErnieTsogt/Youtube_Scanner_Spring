package team.jndk.praktyki.praktyki_spring.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.jndk.praktyki.praktyki_spring.model.data.Channel;

import java.util.List;

@Service
public class ScanSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(ScanSchedulerService.class);

    @Autowired
    private DaoService daoService;

    @Autowired
    private ytService ytService;

    // Triggered by AutoScanSchedulerService according to selected frequency
    public void scheduledScan() {
        List<Channel> channels = daoService.getAllChannels();
        log.info("Scheduled scan: found {} channels", channels.size());
        for (Channel c : channels) {
            try {
                ytService.fetchAndSaveVideos(c.getGoogleId());
            } catch (Exception e) {
                log.warn("Error scanning channel {}: {}", c.getGoogleId(), e.getMessage());
            }
        }
    }
}

