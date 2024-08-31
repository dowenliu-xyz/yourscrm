package cn.yourscrm.mono;

import org.springframework.boot.SpringApplication;

public class TestYourscrmMonoServerApplication {

    public static void main(String[] args) {
        SpringApplication.from(YourscrmMonoServerApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
