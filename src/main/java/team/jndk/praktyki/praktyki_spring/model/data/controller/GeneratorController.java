package team.jndk.praktyki.praktyki_spring.model.data.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import team.jndk.praktyki.praktyki_spring.model.data.Channel;
import team.jndk.praktyki.praktyki_spring.model.data.Video;
import team.jndk.praktyki.praktyki_spring.model.data.service.DaoService;

import java.util.List;

@RestController
public class GeneratorController {

    @Autowired
    private DaoService daoService;

    @GetMapping("/channels")
    public List<Channel> kanal() { //wyswietlanie zawartosc
        System.out.println("Received GET request!!");
        return daoService.getAllChannels();
    }
    @GetMapping("/videos")
    public List<Video> film() { //wyswietlanie zawartosc
        System.out.println("Received GET request!!");
        return daoService.getAllVideos();
    }

    @PostMapping("/start")
    public String start() { // startuje generator
        System.out.println("Received POST request!!");
        daoService.startScan();
        return "Data generated!";
    }
// GET z konkretnym channelem i jego zawartosc
}