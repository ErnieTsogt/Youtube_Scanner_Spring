package team.jndk.praktyki.praktyki_spring.model.data;

import jakarta.persistence.*;

@Table(name = "video_snapshots")
@Entity
public class VideoSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id")
    private YTVideo video;

    @Column(name = "snapshot_date")
    private long snapshotDate;

    private int views;
    private int likes;
    private int comments;

    public VideoSnapshot() {}

    public VideoSnapshot(YTVideo video, long snapshotDate, int views, int likes, int comments) {
        this.video = video;
        this.snapshotDate = snapshotDate;
        this.views = views;
        this.likes = likes;
        this.comments = comments;
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

    public long getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(long snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }
}
