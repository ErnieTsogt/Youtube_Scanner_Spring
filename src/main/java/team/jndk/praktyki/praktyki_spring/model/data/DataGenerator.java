package team.jndk.praktyki.praktyki_spring.model.data;

import org.springframework.stereotype.Component;

import java.util.*;
@Component

public class DataGenerator {

    private static final long TWO_DAYS_MILLIS = 172800000;
    public void main(String[] args) {

        int numChannels = 2;

        Channel channel = new Channel("channel7", "id");
        List<Channel> channels = generateChannels(numChannels);

        System.out.println(channels);
        System.exit(0);

    }

    public List<Channel> generateChannels(int numChannels) {
        List<Channel> channels = new ArrayList<>();

        for (int i = 1; i <= numChannels; i++) {
            String channelName = "Channel" + i;
            String googleID = "googleID" + i;
            Channel channel = new Channel(channelName,googleID );
            channels.add(channel);
        }
        populateVideos(channels);
        return channels;
    }

    private void populateVideos(List<Channel> channels) {
        Random random = new Random();

        for (Channel channel : channels) {
            int numVideos = 5;
            int i;
            String id1 = UUID.randomUUID().toString();
            String id2 = UUID.randomUUID().toString();
            String videoTitle = "";
            for (i = 1; i <= numVideos; i++) {
                int randomNumber = random.nextInt(2);
                String id;
                if (channel.getGoogleId().equals("Channel1")) {
                    if (i <= 3) {
                        videoTitle = "Tech";
                    } else {
                        videoTitle = "Kod";
                    }
                } else if (channel.getGoogleId().equals("Channel2")) {
                    if (i <= 3) {
                        videoTitle = "Java";
                    } else {
                        videoTitle = "Scala";
                    }
                }

                if (randomNumber == 0) {
                    id = id1; // Pierwszy kod UUID
                } else {
                    id = id2; // Drugi kod UUID
                }
                YTVideo previous = channel.getYTVideos()
                        .stream()
                        .filter(vid -> vid.getGoogleId().equals(id))
                        .sorted(Comparator.comparingLong(YTVideo::getScannedDate).reversed())
                        .findFirst()
                        .orElse(null);
                int viewsLast = previous == null ? 1000 : previous.getViews();
                int views = random.nextInt(viewsLast, viewsLast + 1000); // Random number of views

                int likes = random.nextInt(5000) + 100; // Random number of likes
                int comments = random.nextInt(500) + 50; // Random number of comments

                int randomDays = random.nextInt(1, 5);
                long scannedDate = previous == null ? random.nextLong(System.currentTimeMillis() - randomDays * TWO_DAYS_MILLIS, System.currentTimeMillis())
                        : random.nextLong(previous.getScannedDate(), System.currentTimeMillis()); // Random scanned date
                YTVideo YTVideo = new YTVideo(videoTitle, id, likes, comments, views, scannedDate);
                channel.addVideos(YTVideo);
            }
        }
    }


}
