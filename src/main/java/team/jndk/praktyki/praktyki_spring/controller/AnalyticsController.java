package team.jndk.praktyki.praktyki_spring.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import team.jndk.praktyki.praktyki_spring.model.dto.ChannelDTO;
import team.jndk.praktyki.praktyki_spring.model.dto.VideoDTO;
import team.jndk.praktyki.praktyki_spring.model.dto.VideoStatsHistoryDTO;
import team.jndk.praktyki.praktyki_spring.service.AnalyticsService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * GET /api/analytics/channels
     * Get all channels for filter dropdown
     */
    @GetMapping("/channels")
    public List<ChannelDTO> getAllChannels() {
        return analyticsService.getAllChannels();
    }

    /**
     * GET /api/analytics/videos?channel=&from=&to=&page=0&size=20
     * Get videos with optional filtering
     */
    @GetMapping("/videos")
    public Page<VideoDTO> filterVideos(
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "views") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        LocalDate fromDate = null;
        LocalDate toDate = null;
        
        try {
            if (from != null && !from.isEmpty()) {
                fromDate = LocalDate.parse(from);
            }
            if (to != null && !to.isEmpty()) {
                toDate = LocalDate.parse(to);
            }
        } catch (Exception e) {
            // Invalid date format, ignore
        }
        
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return analyticsService.filterVideos(channel, fromDate, toDate, pageable);
    }

    /**
     * GET /api/analytics/video-stats-history?channel=&from=&to=&page=0&size=20
     * Get historical snapshots of video stats (views/likes/comments)
     */
    @GetMapping("/video-stats-history")
    public Page<VideoStatsHistoryDTO> getVideoStatsHistory(
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "snapshotDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        LocalDate fromDate = null;
        LocalDate toDate = null;

        try {
            if (from != null && !from.isEmpty()) {
                fromDate = LocalDate.parse(from);
            }
            if (to != null && !to.isEmpty()) {
                toDate = LocalDate.parse(to);
            }
        } catch (Exception e) {
            // Invalid date format, ignore
        }

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return analyticsService.getVideoStatsHistory(channel, fromDate, toDate, pageable);
    }

    /**
     * GET /api/analytics/trends?channel=&metric=views&days=30
     * Get trend data for line chart
     */
    @GetMapping("/trends")
    public Map<String, Object> getTrends(
            @RequestParam(required = false) String channel,
            @RequestParam(defaultValue = "views") String metric,
            @RequestParam(defaultValue = "30") Integer days) {
        return analyticsService.getTrends(channel, metric, days);
    }

    /**
     * GET /api/analytics/statistics?channel=
     * Get overall statistics
     */
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics(
            @RequestParam(required = false) String channel) {
        return analyticsService.getStatistics(channel);
    }

    /**
     * GET /api/analytics/comparison
     * Get channel comparison data
     */
    @GetMapping("/comparison")
    public List<Map<String, Object>> compareChannels(
            @RequestParam(required = false) String channel,
            @RequestParam(defaultValue = "views") String metric) {
        return analyticsService.compareChannels(channel, metric);
    }

    /**
     * DELETE /api/analytics/channel/{googleId}/videos
     * Delete all videos/scans for a specific channel
     */
    @DeleteMapping("/channel/{googleId}/videos")
    public Map<String, Object> deleteChannelVideos(@PathVariable String googleId) {
        int deletedCount = analyticsService.deleteChannelVideos(googleId);
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("deletedCount", deletedCount);
        response.put("message", "Usunięto " + deletedCount + " skanów kanału: " + googleId);
        return response;
    }

    /**
     * DELETE /api/analytics/channel/{googleId}
     * Delete channel from list/view with all its scans
     */
    @DeleteMapping("/channel/{googleId}")
    public Map<String, Object> deleteChannel(@PathVariable String googleId) {
        long deleted = analyticsService.deleteChannel(googleId);
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("deleted", deleted);
        response.put("message", deleted > 0
                ? "Kanał został usunięty: " + googleId
                : "Kanał nie istnieje: " + googleId);
        return response;
    }
}
