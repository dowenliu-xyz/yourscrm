package cn.yourscrm.mono.auth.adaptor.jwt;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@RequiredArgsConstructor
@Configuration
public class RedisPubSubConfig {
    private final RedisMessageListenerContainer container;
    private final SignKeyRotatedRedisPubSubListener listener;
    private final RedisPubSubProps props;

    @PostConstruct
    public void registerListener() {
        container.addMessageListener(
                new MessageListenerAdapter(listener, "onMessage"),
                ChannelTopic.of(props.getSignKeyRotatedChannel()));
    }
}
