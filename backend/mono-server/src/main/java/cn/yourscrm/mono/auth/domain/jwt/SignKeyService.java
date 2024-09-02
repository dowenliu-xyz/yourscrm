package cn.yourscrm.mono.auth.domain.jwt;

import cn.yourscrm.common.id.IdGenerator;
import jakarta.annotation.PostConstruct;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
public class SignKeyService {
    private final AtomicReference<Map<Long, SignKey>> inMemory = new AtomicReference<>();
    private final SignKeyRepo signKeyRepo;
    private final SignKeyRotatedPublisher publisher;
    private final IdGenerator jwtSignKeyIdGenerator;

    public SignKeyService(SignKeyRepo signKeyRepo,
                          SignKeyRotatedPublisher publisher,
                          @Qualifier("jwtSignKeyIdGenerator")
                          IdGenerator jwtSignKeyIdGenerator) {
        this.signKeyRepo = signKeyRepo;
        this.publisher = publisher;
        this.jwtSignKeyIdGenerator = jwtSignKeyIdGenerator;
    }

    @PostConstruct
    public void loadAllSignKeysToMemory() {
        inMemory.set(signKeyRepo.loadAll().stream()
                .collect(Collectors.toUnmodifiableMap(SignKey::id, s -> s)));
        if (inMemory.get().values().stream().noneMatch(SignKey::canSignToken)) {
            performRotate();
        }
    }

    @NotNull
    public SignKey getOneForSigning() throws NoSuchElementException {
        Optional<SignKey> any = inMemory.get().values().stream()
                .filter(SignKey::canSignToken).findAny();
        return any.orElseGet(this::performRotate);
    }

    @Nullable
    public SignKey findByKeyId(long id) {
        return inMemory.get().get(id);
    }

    public SignKey performRotate() {
        SignKey newKey = newKey();
        signKeyRepo.newKey(newKey);
        List<Long> cleanUpIds = inMemory.get().values().stream()
                .filter(k -> !k.canVerifyToken()).map(SignKey::id).toList();
        signKeyRepo.dropKeys(cleanUpIds);
        publisher.publish();
        Map<Long, SignKey> map = new HashMap<>();
        map.put(newKey.id(), newKey);
        inMemory.get().forEach((k, v) -> {
            if (v.canVerifyToken()) {
                map.put(k, v);
            }
        });
        inMemory.set(Map.copyOf(map));
        return newKey;
    }

    @NotNull
    private SignKey newKey() {
        long keyId = jwtSignKeyIdGenerator.nextId().asLong();
        SecureRandom random = new SecureRandom();
        byte[] secret = new byte[32];
        random.nextBytes(secret);
        LocalDateTime expireDate = LocalDateTime.now().plusWeeks(2);
        LocalDateTime tolerateUntil = expireDate.plusDays(2);
        return new SignKey(keyId, new String(Hex.encode(secret)), expireDate, tolerateUntil, 0);
    }
}
