package art.lapov.vavapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class VavapiApplication {

    public static void main(String[] args) {
        SpringApplication.run(VavapiApplication.class, args);
    }

}
