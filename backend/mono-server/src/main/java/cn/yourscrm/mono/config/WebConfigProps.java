package cn.yourscrm.mono.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cn.yourscrm.mono.web")
@Getter
@Setter
public class WebConfigProps {
    private String traceIdResponseHeaderName = "X-Trace-Id";
}
