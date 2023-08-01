package team.jndk.praktyki.praktyki_spring.model.data;


import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;


@Table(name = "Channels")
@Entity
@Data
@RequiredArgsConstructor
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int id;

    @NotBlank
    @Column(name = "GoogleChanID")
    private final String googleId;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(referencedColumnName = "GoogleChanID", name = "ChanID")
    private final Set<Video> videos = new HashSet<>();

    private Channel() {
        googleId = "";
    }

    public void addVideos(Video... videos) {
        this.videos.addAll(Arrays.asList(videos));
    }

    @Override
    public String toString() {
        return "Channel{" +
                "id='" + id + "'\n" +
                "googleId='" + googleId + "'\n" +
                ", videos=\n" + videos.stream().sorted(Comparator.comparingLong(Video::getScannedDate)).toList() +
                "} \n\n";
    }
}

