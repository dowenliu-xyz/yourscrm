package cn.yourscrm.mono.auth.adaptor.jwt;

import cn.yourscrm.mono.auth.domain.jwt.SignKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SignKeyRotatedRedisPubSubListener {
    private final SignKeyService signKeyService;

    @SuppressWarnings("unused")
    public void onMessage(byte[] ignored) {
        signKeyService.loadAllSignKeysToMemory();
    }
}
