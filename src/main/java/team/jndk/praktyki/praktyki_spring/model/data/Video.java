package team.jndk.praktyki.praktyki_spring.model.data;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.*;
import java.util.Date;

@Table(name = "Videos")
@Data
@RequiredArgsConstructor
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @NotBlank
    private final String title;
    @NotBlank
    private final String googleId;
    @Min(0)
    private final int views;
    @Min(0)
    private final int likes;
    @Min(0)
    private final int comments;
    @NotBlank
    private final long scannedDate;

    private Video() {
        title = "";
        googleId = "";
        views = Integer.MIN_VALUE;
        likes = Integer.MIN_VALUE;
        comments = Integer.MIN_VALUE;
        scannedDate = Long.MIN_VALUE;
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
