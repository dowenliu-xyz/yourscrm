package cn.yourscrm.mono;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

@SpringBootApplication
@Modulithic(sharedModules = {"config", "ddd", "time"})
public class YourscrmMonoServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourscrmMonoServerApplication.class, args);
    }
}
