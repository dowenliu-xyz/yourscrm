package cn.yourscrm.mono.auth.adaptor.jwt;

import cn.yourscrm.common.id.IdGenerator;
import cn.yourscrm.common.id.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static cn.yourscrm.common.id.SnowflakeIdGenerator.Builder.DEFAULT_NODE_ID_BITS;

@RequiredArgsConstructor
@Configuration
public class JwtConfig {
    @Bean
    public IdGenerator jwtSignKeyIdGenerator() {
        return SnowflakeIdGenerator.builder()
                .nodeIdBits(DEFAULT_NODE_ID_BITS - 4)
                .build();
    }
}
