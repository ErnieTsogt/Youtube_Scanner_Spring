package team.jndk.praktyki.praktyki_spring.model.dto;

public class MonitoringAlertDTO {
    public Long id;
    public Integer videoId;
    public String videoTitle;
    public String videoGoogleId;
    public String channelName;
    public String channelGoogleId;
    public String alertType;
    public String severity;
    public String message;
    public Long detectedAt;
    public boolean acknowledged;
}