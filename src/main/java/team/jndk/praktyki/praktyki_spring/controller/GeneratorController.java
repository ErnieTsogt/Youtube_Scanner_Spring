package team.jndk.praktyki.praktyki_spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.jndk.praktyki.praktyki_spring.model.dto.ChannelDTO;
import team.jndk.praktyki.praktyki_spring.model.dto.VideoDTO;
import team.jndk.praktyki.praktyki_spring.model.mapper.DtoMapper;
import team.jndk.praktyki.praktyki_spring.service.DaoService;
import team.jndk.praktyki.praktyki_spring.service.ytService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class GeneratorController {

    @Autowired
    private DaoService daoService;
    @Autowired
    private ytService ytService;

    @GetMapping("/channels")
    public List<ChannelDTO> canal() {
        System.out.println("Received GET request!!");
        return daoService.getAllChannels().stream().map(DtoMapper::toChannelDTO).collect(Collectors.toList());
    }

    @GetMapping("/videos")
    public List<VideoDTO> film() {
        System.out.println("Received GET request!!");
        return daoService.getAllVideos().stream().map(DtoMapper::toVideoDTO).collect(Collectors.toList());
    }

    @PostMapping ("/start/{channelId}")
    public ResponseEntity<String> fetchAndSaveVideos(@PathVariable String channelId) {
        try {
            ytService.fetchAndSaveVideos(channelId);
            return ResponseEntity.ok("Videos fetched and saved for channel ID: " + channelId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : "Błąd podczas skanowania kanału";
            if (message.toLowerCase().contains("quota")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(message);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
        }
    }

    @PostMapping("/api/start/{channelId}")
    public ResponseEntity<String> fetchAndSaveVideosApi(@PathVariable String channelId) {
        try {
            ytService.fetchAndSaveVideos(channelId);
            return ResponseEntity.ok("Videos fetched and saved for channel ID: " + channelId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : "Błąd podczas skanowania kanału";
            if (message.toLowerCase().contains("quota")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(message);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
        }
    }

    @PostMapping("/api/start")
    public ResponseEntity<String> fetchAndSaveVideosApiQuery(@RequestParam String channelId) {
        try {
            ytService.fetchAndSaveVideos(channelId);
            return ResponseEntity.ok("Videos fetched and saved for channel ID: " + channelId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : "Błąd podczas skanowania kanału";
            if (message.toLowerCase().contains("quota")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(message);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
        }
    }

    @PostMapping("/api/start/video")
    public ResponseEntity<String> fetchAndSaveSingleVideo(
            @RequestParam String channelId,
            @RequestParam String videoId) {
        try {
            ytService.fetchAndSaveSingleVideo(channelId, videoId);
            return ResponseEntity.ok("Video fetched and saved for channel ID: " + channelId + ", video ID: " + videoId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : "Błąd podczas skanowania filmu";
            if (message.toLowerCase().contains("quota")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(message);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
        }
    }
}
