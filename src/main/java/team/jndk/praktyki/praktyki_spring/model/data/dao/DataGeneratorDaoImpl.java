package team.jndk.praktyki.praktyki_spring.model.data.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import team.jndk.praktyki.praktyki_spring.model.data.Channel;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DataGeneratorDaoImpl implements DataGeneratorDao {


    private File dataFile;
    private ObjectMapper objectMapper;

    public DataGeneratorDaoImpl(String filePath) {
        dataFile = new File(filePath);
        this.objectMapper = new ObjectMapper();
    }


    @Override
    public void saveChannels(List<Channel> channels) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFile, channels);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
