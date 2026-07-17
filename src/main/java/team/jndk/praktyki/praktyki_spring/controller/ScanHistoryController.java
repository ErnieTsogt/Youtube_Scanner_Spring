package team.jndk.praktyki.praktyki_spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.jndk.praktyki.praktyki_spring.model.dto.ScanHistoryDTO;
import team.jndk.praktyki.praktyki_spring.model.mapper.DtoMapper;
import team.jndk.praktyki.praktyki_spring.repository.ScanHistoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/scan-history")
public class ScanHistoryController {

    @Autowired
    private ScanHistoryRepository scanHistoryRepository;

    @GetMapping
    public List<ScanHistoryDTO> list(@RequestParam(required = false) Integer channelId) {
        if (channelId == null) {
            return scanHistoryRepository.findAll().stream().map(DtoMapper::toScanHistoryDTO).collect(Collectors.toList());
        } else {
            return scanHistoryRepository.findAll().stream().filter(h -> h.getChannel() != null && h.getChannel().getId() == channelId).map(DtoMapper::toScanHistoryDTO).collect(Collectors.toList());
        }
    }
}
