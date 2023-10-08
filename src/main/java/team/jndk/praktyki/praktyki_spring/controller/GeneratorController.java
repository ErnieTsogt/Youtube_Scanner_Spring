package team.jndk.praktyki.praktyki_spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
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

    @PostMapping ("/start")
    public String fetchAndSaveVideos() {
        ytService.fetchAndSaveVideos();
        return "Videos fetched and saved!";
    }
}
