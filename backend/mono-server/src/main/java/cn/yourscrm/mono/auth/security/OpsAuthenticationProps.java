package cn.yourscrm.mono.auth.security;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Component
@ConfigurationProperties(prefix = "cn.yourscrm.mono.auth.security.ops")
public class OpsAuthenticationProps {
    @Setter
    private List<String> allowIPs = List.of("127.0.0.1");

    private List<IpAddressMatcher> matchers;

    @PostConstruct
    public void init() {
        matchers = allowIPs.stream().map(IpAddressMatcher::new).toList();
    }
}
