package cn.yourscrm.mono;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.core.ApplicationModules;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class YourscrmMonoServerApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void verifyStructure() {
        ApplicationModules modules = ApplicationModules.of(YourscrmMonoServerApplication.class);
        modules.forEach(System.out::println);
        modules.verify();
    }

}
