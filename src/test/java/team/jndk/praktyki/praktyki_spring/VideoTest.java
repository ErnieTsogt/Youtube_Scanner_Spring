package team.jndk.praktyki.praktyki_spring;

import lombok.NonNull;
import org.junit.Ignore;
import org.junit.Test;
import team.jndk.praktyki.praktyki_spring.model.data.YTVideo;

import static junit.framework.TestCase.assertEquals;

public class VideoTest {

    @Test

    public void testGettingAndSettingTitle() {
        String title = "title";
        YTVideo YTVideo = new YTVideo(title, "Lekcja1", 20000, 20, 10, 1231223123L);
        assertEquals(title, YTVideo.getTitle());
    }

    @Test
    @NonNull
    @Ignore
    public void testTitleValues() {
        String title = null;
        YTVideo YTVideo = new YTVideo(title, "Lekcja1", 20000, 20, 10, 1231223123L);
    }

    @Test

    public void testIdValues() {
        String value = "";
        YTVideo YTVideo = new YTVideo("JavaTutorial", value, 20000, 20, 10, 1231223123L);
    }


    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void testViewsValueNegative() {
        int value = -8;
        new YTVideo("JavaTutorial", "Lekcja1", value, 20, 10, 1231223123L);
    }

    @Test

    public void testViewsValueZero() {
        int value = 0;
        YTVideo YTVideo = new YTVideo("JavaTutorial", "Lekcja1", value, 20, 10, 1231223123L);
        assertEquals(value, YTVideo.getViews());
    }

    @Test

    public void testViewsValuePositive() {
        int value = 13;
        YTVideo YTVideo = new YTVideo("JavaTutorial", "Lekcja1", value, 20, 10, 1231223123L);
        assertEquals(value, YTVideo.getViews());
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void testLikesValueNegative() {
        int value = -8;
        YTVideo YTVideo = new YTVideo("JavaTutorial", "Lekcja1", 20000, value, 10, 1231223123L);
    }

    @Test
    public void testLikesValueZero() {
        int value = 0;
        YTVideo YTVideo = new YTVideo("JavaTutorial", "Lekcja1", 20000, value, 10, 1231223123L);
        assertEquals(value, YTVideo.getLikes());
    }

    @Test

    public void testLikesValuePositive() {
        int value = 13;
        YTVideo YTVideo = new YTVideo("JavaTutorial", "Lekcja1", 20000, value, 10, 1231223123L);
        assertEquals(value, YTVideo.getLikes());
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void testCommentsValueNegative() {
        int value = -8;
        YTVideo YTVideo = new YTVideo("JavaTutorial", "Lekcja1", 20000, 20, value, 1231223123L);
    }

    @Test

    public void testCommentsValueZero() {
        int value = 0;
        YTVideo YTVideo = new YTVideo("JavaTutorial", "Lekcja1", 20000, 20, value, 1231223123L);
        assertEquals(value, YTVideo.getComments());
    }

    @Test

    public void testCommentsValuePositive() {
        int value = 13;
        YTVideo YTVideo = new YTVideo("JavaTutorial", "Lekcja1", 20000, 20, value, 1231223123L);
        assertEquals(value, YTVideo.getComments());
    }

    @Test
    @NonNull
    @Ignore
    public void testScannedDataValues() {
        long value = 0;
        YTVideo YTVideo = new YTVideo("JavaTutorial", "Lekcja1", 20000, 20, 10, value);
    }
}