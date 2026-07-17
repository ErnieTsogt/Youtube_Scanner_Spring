package team.jndk.praktyki.praktyki_spring.model.data;

import jakarta.persistence.*;

@Entity
@Table(name = "monitoring_alerts")
public class MonitoringAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id")
    private YTVideo video;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chan_id")
    private Channel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(name = "detected_at", nullable = false)
    private long detectedAt;

    @Column(nullable = false)
    private boolean acknowledged;

    public MonitoringAlert() {
    }

    public MonitoringAlert(YTVideo video, Channel channel, AlertType alertType, AlertSeverity severity, String message, long detectedAt) {
        this.video = video;
        this.channel = channel;
        this.alertType = alertType;
        this.severity = severity;
        this.message = message;
        this.detectedAt = detectedAt;
        this.acknowledged = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public YTVideo getVideo() {
        return video;
    }

    public void setVideo(YTVideo video) {
        this.video = video;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(long detectedAt) {
        this.detectedAt = detectedAt;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }
}