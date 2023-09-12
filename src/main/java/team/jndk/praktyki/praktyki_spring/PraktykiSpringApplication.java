package team.jndk.praktyki.praktyki_spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = "classpath:application.properties")
public class PraktykiSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(PraktykiSpringApplication.class, args);
	}

}
