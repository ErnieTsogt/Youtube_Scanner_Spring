package team.jndk.praktyki.praktyki_spring.controller;

import org.springframework.web.bind.annotation.*;
import team.jndk.praktyki.praktyki_spring.model.dto.MonitoringAlertConfigDTO;
import team.jndk.praktyki.praktyki_spring.model.dto.MonitoringAlertDTO;
import team.jndk.praktyki.praktyki_spring.service.MonitoringAlertService;

import java.util.List;

@RestController
@RequestMapping("/api/analytics/alerts")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AlertController {

    private final MonitoringAlertService monitoringAlertService;

    public AlertController(MonitoringAlertService monitoringAlertService) {
        this.monitoringAlertService = monitoringAlertService;
    }

    @GetMapping
    public List<MonitoringAlertDTO> getAlerts(
            @RequestParam(required = false) String channel,
            @RequestParam(required = false, defaultValue = "false") Boolean acknowledged,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return monitoringAlertService.getRecentAlerts(channel, acknowledged, limit);
    }

    @PostMapping("/{id}/acknowledge")
    public MonitoringAlertDTO acknowledgeAlert(@PathVariable Long id) {
        return monitoringAlertService.acknowledgeAlert(id);
    }

    @GetMapping("/config")
    public MonitoringAlertConfigDTO getConfig() {
        return monitoringAlertService.getConfig();
    }

    @PutMapping("/config")
    public MonitoringAlertConfigDTO updateConfig(@RequestBody MonitoringAlertConfigDTO config) {
        return monitoringAlertService.updateConfig(config);
    }
}