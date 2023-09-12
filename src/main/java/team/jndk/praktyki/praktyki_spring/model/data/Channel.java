package team.jndk.praktyki.praktyki_spring.model.data;


import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.*;


@Table(name = "Channels")
@Entity
@Data
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @NotBlank
    @Column(name = "channel_names")
    private String ChannelName;
    @NotBlank
    @Column(name = "google_chan_id")
    private String googleId;



    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(referencedColumnName = "id", name = "chan_id")
    private final Set<YTVideo> YTVideos = new HashSet<>();

    public Channel(String channelName, String googleId) {
        this.ChannelName =channelName ;
        this.googleId = googleId;
    }

    public Channel() {

    }

    public void addVideos(YTVideo... YTVideos) {
        this.YTVideos.addAll(Arrays.asList(YTVideos));
    }

    @Override
    public String toString() {
        return "Channel{" +
                "id='" + id + "'\n" +
                "googleId='" + googleId + "'\n" +
                ", videos=\n" + YTVideos.stream().sorted(Comparator.comparingLong(YTVideo::getScannedDate)).toList() +
                "} \n\n";
    }
}

