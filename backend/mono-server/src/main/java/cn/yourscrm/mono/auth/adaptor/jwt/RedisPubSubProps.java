package cn.yourscrm.mono.auth.adaptor.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cn.yourscrm.mono.auth.jwt.redis-pub-sub")
@Getter
@Setter
public class RedisPubSubProps {
    private String signKeyRotatedChannel = "jwt-sign-key-rotated";
}
