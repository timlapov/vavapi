package art.lapov.vavapi;

import art.lapov.vavapi.repository.LocationRepository;
import art.lapov.vavapi.repository.StationRepository;
import art.lapov.vavapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class VavapiApplicationTests {

    @Autowired
    private ApplicationContext context;


    @Test
    void contextLoads() {
        assertNotNull(context);
    }

    @Test
    void allRepositoriesAreLoaded() {
        assertNotNull(context.getBean(UserRepository.class));
        assertNotNull(context.getBean(LocationRepository.class));
        assertNotNull(context.getBean(StationRepository.class));
    }

}
