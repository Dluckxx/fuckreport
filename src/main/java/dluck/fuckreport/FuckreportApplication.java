package dluck.fuckreport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FuckreportApplication {

	public static void main(String[] args) {
		SpringApplication.run(FuckreportApplication.class, args);
	}

}
