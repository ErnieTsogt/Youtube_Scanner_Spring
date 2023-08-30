package team.jndk.praktyki.praktyki_spring.model.data;

import com.google.api.client.util.DateTime;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.*;
import java.util.Date;

@Table(name = "Videos")
@Entity
@Data
public class YTVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @NotBlank
    private final String title;
    @NotBlank
    @Column(name = "google_vid_id")
    private final String googleId;
    @Min(0)
    private final int likes;
    @Min(0)
    private final int comments;
    @Min(0)
    private final int views;
    @NotBlank
    @Column(name = "scanned_date")
    private final long scannedDate;

    public YTVideo(String title, String videoId, int likes, int comments, int views, long scannedDate) {
        this.title = "";
        googleId = "";
        this.views = Integer.MIN_VALUE;
        this.likes = Integer.MIN_VALUE;
        this.comments = Integer.MIN_VALUE;
        this.scannedDate = Long.MIN_VALUE;
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
