package cn.yourscrm.mono.auth.adaptor.jwt;

import cn.yourscrm.mono.auth.domain.jwt.SignKeyRotatedPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class SignKeyRotatedRedisPubSubPublisher implements SignKeyRotatedPublisher {
    private final RedisOperations<String, String> redisOperations;
    private final RedisPubSubProps props;

    @Override
    public void publish() {
        try {
            redisOperations.convertAndSend(
                    props.getSignKeyRotatedChannel(), "rotated");
        } catch (Exception e) {
            log.warn("Failed to publish sign key rotated event", e);
        }
    }
}
