package team.jndk.praktyki.praktyki_spring.model.data;


import jakarta.persistence.*;

import jakarta.validation.constraints.NotBlank;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonManagedReference;


@Table(name = "channels")
@Entity
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotBlank
    @Column(name = "channel_names")
    private String channelName;
    @NotBlank
    @Column(name = "google_chan_id", unique = true)
    private String googleId;

    @Column(name = "subscribers")
    private long subscribers;

    // OneToMany mappedBy to 'channel' in YTVideo. Use LAZY fetch and cascade all so persisting Channel persists videos.
    @JsonManagedReference
    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final Set<YTVideo> ytVideos = new HashSet<>();

    public Channel(String channelName, String googleId) {
        this.channelName = channelName;
        this.googleId = googleId;
    }

    public Channel(String channelName, String googleId, long subscribers) {
        this.channelName = channelName;
        this.googleId = googleId;
        this.subscribers = subscribers;
    }

    public Channel() {

    }

    public void addVideos(YTVideo... videos) {
        this.ytVideos.addAll(Arrays.asList(videos));
        for (YTVideo v : videos) {
            v.setChannel(this);
        }
    }

    @Override
    public String toString() {
        return "Channel{" +
                "id='" + id + "'\n" +
                "googleId='" + googleId + "'\n" +
                ", videos=\n" + ytVideos.stream().sorted(Comparator.comparingLong(YTVideo::getScannedDate)).toList() +
                "} \n\n";
    }

    // Jawne metody dostępowe (gettery) używane w innych klasach
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public Set<YTVideo> getYtVideos() {
        return ytVideos;
    }

    public long getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(long subscribers) {
        this.subscribers = subscribers;
    }
}
