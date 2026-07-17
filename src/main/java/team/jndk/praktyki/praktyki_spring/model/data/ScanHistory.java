package team.jndk.praktyki.praktyki_spring.model.data;

import jakarta.persistence.*;

@Table(name = "scan_history")
@Entity
public class ScanHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chan_id")
    private Channel channel;

    private long scanDate;

    @Enumerated(EnumType.STRING)
    private ScanStatus status;

    @Column(length = 2000)
    private String message;

    private int videosSaved;

    public ScanHistory() {}

    public ScanHistory(Channel channel, long scanDate, ScanStatus status, String message, int videosSaved) {
        this.channel = channel;
        this.scanDate = scanDate;
        this.status = status;
        this.message = message;
        this.videosSaved = videosSaved;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public long getScanDate() {
        return scanDate;
    }

    public void setScanDate(long scanDate) {
        this.scanDate = scanDate;
    }

    public ScanStatus getStatus() {
        return status;
    }

    public void setStatus(ScanStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getVideosSaved() {
        return videosSaved;
    }

    public void setVideosSaved(int videosSaved) {
        this.videosSaved = videosSaved;
    }
}
