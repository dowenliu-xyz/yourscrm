package cn.yourscrm.ops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.CommandScan;

@SpringBootApplication
@CommandScan
public class OpsCliApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpsCliApplication.class, args);
    }

}