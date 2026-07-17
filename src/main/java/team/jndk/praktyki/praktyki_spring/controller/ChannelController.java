package team.jndk.praktyki.praktyki_spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.jndk.praktyki.praktyki_spring.model.dto.ChannelDTO;
import team.jndk.praktyki.praktyki_spring.model.mapper.DtoMapper;
import team.jndk.praktyki.praktyki_spring.service.DaoService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/channels")
public class ChannelController {

    @Autowired
    private DaoService daoService;

    @GetMapping
    public List<ChannelDTO> getAllChannels() {
        return daoService.getAllChannels().stream().map(DtoMapper::toChannelDTO).collect(Collectors.toList());
    }
}

