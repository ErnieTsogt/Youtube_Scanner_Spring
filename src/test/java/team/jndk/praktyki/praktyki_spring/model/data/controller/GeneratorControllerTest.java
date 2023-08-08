package team.jndk.praktyki.praktyki_spring.model.data.controller;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.test.web.servlet.MockMvc;
import team.jndk.praktyki.praktyki_spring.model.data.Channel;
import team.jndk.praktyki.praktyki_spring.model.data.service.DaoService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        String channelId = "Channel1";
        // Przykładowa lista kanałów do zwrócenia przez serwis DAO
        List<Channel> channels = Arrays.asList(new Channel( "Channel1", "Channel1"), new Channel("","Channel2"));
        when(daoService.getAllChannels()).thenReturn(channels);

        // When/Then
        mockMvc.perform(get("/channels/" + channelId))
                .andExpect(status().isOk());
        // Możesz także dodać dodatkowe asercje, aby sprawdzić odpowiedź serwera na przykład zawartość odpowiedzi.
    }

    @Test
    void film() {
    }

    @Test
    void start() throws Exception {
        daoService.startScan();
        mockMvc.perform(post("/start")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}