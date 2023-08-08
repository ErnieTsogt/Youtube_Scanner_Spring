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
    @Column(name = "ChannelNames")
    private final String ChannelName;
    @NotBlank
    @Column(name = "GoogleChanID")
    private final String googleId;


    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(referencedColumnName = "ID", name = "ChanID")
    private final Set<Video> videos = new HashSet<>();

    public Channel() {
        ChannelName = "";
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

