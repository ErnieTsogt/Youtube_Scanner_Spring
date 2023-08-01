package team.jndk.praktyki.praktyki_spring;

import lombok.extern.slf4j.Slf4j;
import team.jndk.praktyki.praktyki_spring.model.data.Channel;
import team.jndk.praktyki.praktyki_spring.model.data.DataGenerator;
import team.jndk.praktyki.praktyki_spring.model.data.dao.DBGeneratorDaoImpl;
import team.jndk.praktyki.praktyki_spring.model.data.dao.DataGeneratorDao;

import javax.validation.constraints.NotBlank;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

@Slf4j
public class Main {

    public static void main(@NotBlank String[] args) {
        String propsFile = args[0];
        Properties prop = new Properties();
        try (FileInputStream input = new FileInputStream(propsFile)) {
            prop.load(input);
        } catch (IOException e) {
            log.error("Failed to load properties file: " + e.getMessage());
            return;
        }

        String url = prop.getProperty("database_url");

        DataGeneratorDao dataSaver = new DBGeneratorDaoImpl(url);

//        List<Channel> channels = DataGenerator.generateChannels(Integer.parseInt(prop.getProperty("num_channels")));


//        dataSaver.saveChannels(channels);
    }


}
