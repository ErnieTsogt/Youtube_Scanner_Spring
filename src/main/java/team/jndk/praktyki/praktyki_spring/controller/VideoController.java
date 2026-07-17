package team.jndk.praktyki.praktyki_spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.jndk.praktyki.praktyki_spring.model.dto.VideoDTO;
import team.jndk.praktyki.praktyki_spring.model.mapper.DtoMapper;
import team.jndk.praktyki.praktyki_spring.service.DaoService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    @Autowired
    private DaoService daoService;

    @GetMapping
    public List<VideoDTO> getAllVideos() {
        return daoService.getAllVideos().stream().map(DtoMapper::toVideoDTO).collect(Collectors.toList());
    }
}

