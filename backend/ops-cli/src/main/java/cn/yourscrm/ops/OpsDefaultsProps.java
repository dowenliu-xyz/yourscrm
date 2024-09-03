package cn.yourscrm.ops;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cn.yourscrm.ops.defaults")
@Getter
@Setter
public class OpsDefaultsProps {
    private String backendBaseUrl = "http://localhost:8080";
    private String dbHost = "localhost";
    private int dbPort = 5432;
    private String dbSchema = "postgres";
    private String dbUsername = "postgres";
    private String dbPassword = "postgres";
}
