package team.jndk.praktyki.praktyki_spring.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import team.jndk.praktyki.praktyki_spring.model.dto.BackupPayloadDTO;
import team.jndk.praktyki.praktyki_spring.service.BackupService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/backup")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class BackupController {

    private static final ZoneId FILE_ZONE = ZoneId.of("Europe/Warsaw");

    private final BackupService backupService;
    private final ObjectMapper objectMapper;

    public BackupController(BackupService backupService, ObjectMapper objectMapper) {
        this.backupService = backupService;
        this.objectMapper = objectMapper;
    }

    @GetMapping(value = "/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> exportBackup() throws Exception {
        BackupPayloadDTO payload = backupService.exportBackup();
        byte[] jsonBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(payload);

        String fileStamp = LocalDateTime.now(FILE_ZONE).format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String fileName = "youtube-scanner-backup-" + fileStamp + ".json";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(jsonBytes);
    }

    @PostMapping(value = "/restore", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> restoreBackup(@RequestParam("file") MultipartFile file) throws Exception {
        BackupPayloadDTO payload = objectMapper.readValue(file.getInputStream(), BackupPayloadDTO.class);
        Map<String, Object> summary = backupService.restoreBackup(payload);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Backup został przywrócony (dopisanie + nadpisanie duplikatów)");
        response.putAll(summary);
        return response;
    }
}
