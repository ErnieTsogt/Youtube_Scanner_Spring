package team.jndk.praktyki.praktyki_spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.*;
import team.jndk.praktyki.praktyki_spring.model.data.Channel;
import team.jndk.praktyki.praktyki_spring.model.data.YTVideo;
import team.jndk.praktyki.praktyki_spring.service.DaoService;
import team.jndk.praktyki.praktyki_spring.service.ytService;

import java.util.List;

@RestController
public class GeneratorController {

    @Autowired
    private DaoService daoService;
    @Autowired
    private ytService ytService;

    @GetMapping("/channels")
    public List<Channel> canal() {
        System.out.println("Received GET request!!");
        return daoService.getAllChannels();
    }

    @GetMapping("/videos")
    public List<YTVideo> film() {
        System.out.println("Received GET request!!");
        return daoService.getAllVideos();
    }

    @PostMapping ("/start/{channelId}")
    public String fetchAndSaveVideos(@PathVariable String channelId) {
        ytService.fetchAndSaveVideos(channelId);
        return "Videos fetched and saved for channel ID: " + channelId;
    }
}
