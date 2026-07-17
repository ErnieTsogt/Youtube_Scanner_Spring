package team.jndk.praktyki.praktyki_spring.config;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class ytConfig {

    @Bean
    public YouTube youTube() {
        // Użycie nowego buildera NetHttpTransport oraz domyślnej instancji JacksonFactory
        NetHttpTransport transport = new NetHttpTransport.Builder().build();
        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        // HttpRequestInitializer z ustawionymi timeoutami i możliwością rozbudowy (np. logowanie)
        HttpRequestInitializer initializer = new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest) throws IOException {
                // 3 minuty timeout domyślnie
                httpRequest.setConnectTimeout(3 * 60 * 1000);
                httpRequest.setReadTimeout(3 * 60 * 1000);
                // tutaj można dodać interceptory lub logowanie
            }
        };

        return new YouTube.Builder(transport, jsonFactory, initializer)
                .setApplicationName("ytDataScanner")
                .build();
    }
}
