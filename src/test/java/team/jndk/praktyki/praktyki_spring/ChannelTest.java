package team.jndk.praktyki.praktyki_spring;

import org.junit.Test;
import team.jndk.praktyki.praktyki_spring.model.data.Channel;
import team.jndk.praktyki.praktyki_spring.model.data.YTVideo;

import static junit.framework.TestCase.assertEquals;

public class ChannelTest {

    @Test
    public void testIdenticalEntries() {
        Channel addVid = new Channel("id","channel123");
        YTVideo YTVideo = new YTVideo("JavaTutorial", "kot", 20000, 20,10,1231223123L);
        //addVid.addVideos(video);
        addVid.addVideos(new YTVideo("JavaTutorial", "pies", 20000, 20,10,1231223123L));
        addVid.addVideos(new YTVideo("JavaTutorial", "pies", 20040, 21,10,1234223123L));

        assertEquals(2, addVid.getYTVideos().size());
    }
}
