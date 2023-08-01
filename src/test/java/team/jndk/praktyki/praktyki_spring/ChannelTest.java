package team.jndk.praktyki.praktyki_spring;

import org.junit.Test;
import team.jndk.praktyki.praktyki_spring.model.data.Channel;
import team.jndk.praktyki.praktyki_spring.model.data.Video;

import static junit.framework.TestCase.assertEquals;

public class ChannelTest {

    @Test
    public void testIdenticalEntries() {
        Channel addVid = new Channel("id");
        Video video = new Video("JavaTutorial", "kot", 20000, 20,10,1231223123L);
        //addVid.addVideos(video);
        addVid.addVideos(new Video("JavaTutorial", "pies", 20000, 20,10,1231223123L));
        addVid.addVideos(new Video("JavaTutorial", "pies", 20040, 21,10,1234223123L));

        assertEquals(2, addVid.getVideos().size());
    }
}
