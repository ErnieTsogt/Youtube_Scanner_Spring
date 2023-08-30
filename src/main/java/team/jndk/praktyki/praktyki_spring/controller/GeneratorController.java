package team.jndk.praktyki.praktyki_spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
    @ResponseBody
    public List<Channel> canal() {
        System.out.println("Received GET request!!");
        return daoService.getAllChannels();
    }

    @GetMapping("/videos")
    public List<YTVideo> film() { //wyswietlanie zawartosc
        System.out.println("Received GET request!!");
        return daoService.getAllVideos();
    }

    @PostMapping("/start")
    public String start() { // startuje generator
        System.out.println("Received POST request!!");
        daoService.startScan();
        return "Data generated!";
    }

    @PostMapping("/generate")
    public String generateVideos() {
        com.google.api.services.youtube.model.Channel channel = new com.google.api.services.youtube.model.Channel();
        channel.setId("adwentyscipszczyna");
        List<YTVideo> generatedVideos = ytService.fetchAndSaveVideos(channel);
        return "Videos generated and saved!";
    }
}
