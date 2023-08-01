package team.jndk.praktyki.praktyki_spring;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import team.jndk.praktyki.praktyki_spring.model.data.Channel;
import team.jndk.praktyki.praktyki_spring.model.data.DataGenerator;
import team.jndk.praktyki.praktyki_spring.model.data.dao.DataGeneratorDao;
import team.jndk.praktyki.praktyki_spring.model.data.dao.DataGeneratorDaoImpl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class DataGeneratorDaoImplTest {
    private static final String TEST_FILE_PATH = "testowy.json";
    private ObjectMapper objectMapper = new ObjectMapper();
    private DataGeneratorDao dataSaver = new DataGeneratorDaoImpl(TEST_FILE_PATH);

    @Test
    public void testSerializacja() throws IOException {
        // Przygotowanie danych testowych
        List<Channel> channels = DataGenerator.generateChannels(3);

        // Serializacja kanałów
        dataSaver.saveChannels(channels);

        // Wczytanie kanałów bezpośrednio z pliku JSON
        List<Channel> loadedChannels = objectMapper.readValue(new File(TEST_FILE_PATH), new TypeReference<List<Channel>>() {
        });

        // Sprawdzenie, czy wczytane kanały są zgodne z oryginalnymi danymi testowymi
        assertEquals(channels.size(), loadedChannels.size());
        for (int i = 0; i < channels.size(); i++) {
            Channel originalChannel = channels.get(i);
            Channel loadedChannel = loadedChannels.get(i);
            assertEquals(originalChannel.getId(), loadedChannel.getId());
        }
    }
}
