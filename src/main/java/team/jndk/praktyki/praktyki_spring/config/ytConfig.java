package team.jndk.praktyki.praktyki_spring.config;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ytConfig {

    @Bean
    public YouTube youTube() {
        return new YouTube.Builder(
                new NetHttpTransport(), new JacksonFactory(), request -> {})
                .setApplicationName("ytDataScanner")
                .build();
    }
}
