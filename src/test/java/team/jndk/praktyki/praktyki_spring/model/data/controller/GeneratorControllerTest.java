package team.jndk.praktyki.praktyki_spring.model.data.controller;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import team.jndk.praktyki.praktyki_spring.controller.GeneratorController;
import team.jndk.praktyki.praktyki_spring.model.data.Channel;
import team.jndk.praktyki.praktyki_spring.model.data.YTVideo;
import team.jndk.praktyki.praktyki_spring.service.DaoService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@Ignore
@WebMvcTest(GeneratorController.class)
class GeneratorControllerTest {

    @MockBean
    private DaoService daoService;
    @Autowired
    private MockMvc mockMvc;
    @Test
    void canal() throws Exception {
        // Given
        List<Channel> channels = Arrays.asList(new Channel( "Channel1", "googleid1"), new Channel("Channel2","googleid2"));
        when(daoService.getAllChannels()).thenReturn(channels);

        // When/Then
        mockMvc.perform(get("/channels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(channels.size()));
    }

    @Test
    void film() throws Exception{
        List<YTVideo> YTVideos = Arrays.asList(new YTVideo("JavaTutorial", "Lekcja1", 13, 20, 10, 1231223123L)
        , new YTVideo("stalin", "421idasd", 13, 450, 3400, 234235235235L));
        when(daoService.getAllVideos()).thenReturn(YTVideos);

        // When/Then
        mockMvc.perform(get("/videos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(YTVideos.size()));
    }

    @Test
    void start() throws Exception {
        daoService.startScan();
        mockMvc.perform(post("/start")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}