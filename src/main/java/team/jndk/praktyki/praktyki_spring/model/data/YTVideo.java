package team.jndk.praktyki.praktyki_spring.model.data;

import jakarta.persistence.*;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.Date;

@Table(name = "YTVideos")
@Entity
@Data
public class YTVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @NotBlank
    private String title;
    @NotBlank
    @Column(name = "google_vid_id")
    private String googleId;
    @Min(0)
    private int likes;
    @Min(0)
    private int comments;
    @Min(0)
    private int views;
    @NotBlank
    @Column(name = "scanned_date")
    private long scannedDate;
    @ManyToOne
    @JoinColumn(name = "chan_id")
    private Channel channel;

    public YTVideo(String title, String videoId, int likes, int comments, int views, long scannedDate) {
        this.title= title;
        this.googleId = videoId;
        this.views = views;
        this.likes = likes;
        this.comments = comments;
        this.scannedDate = scannedDate;
    }

    public YTVideo() {

    }


    @Override
    public String toString() {
        return "Video{" +
                "title='" + title + "'\n" +
                ", id='" + googleId + "'\n" +
                ", views=" + views + "'\n" +
                ", likes=" + likes + "'\n" +
                ", comments=" + comments + "'\n" +
                ", scannedDate='" + new Date(scannedDate).toGMTString() +
                "}\n\n";
    }
}
