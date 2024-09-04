package cn.yourscrm.mono.auth.domain.jwt;

import cn.yourscrm.common.id.ID;
import cn.yourscrm.common.id.IdGenerator;
import cn.yourscrm.mono.time.Nower;
import jakarta.annotation.PostConstruct;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
public class SignKeyService {
    private final AtomicReference<Map<ID, SignKey>> inMemory = new AtomicReference<>();
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
                .collect(Collectors.toUnmodifiableMap(SignKey::getId, s -> s)));
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
    public SignKey findByKeyId(ID id) {
        return inMemory.get().get(id);
    }

    public SignKey performRotate() {
        SignKey newKey = newKey();
        signKeyRepo.newKey(newKey);
        List<ID> cleanUpIds = inMemory.get().values().stream()
                .filter(k -> !k.canVerifyToken()).map(SignKey::getId).toList();
        signKeyRepo.dropKeys(cleanUpIds);
        publisher.publish();
        Map<ID, SignKey> map = new HashMap<>();
        map.put(newKey.getId(), newKey);
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
        ID keyId = jwtSignKeyIdGenerator.nextId();
        ID system = jwtSignKeyIdGenerator.zero();
        SecureRandom random = new SecureRandom();
        byte[] secret = new byte[32];
        random.nextBytes(secret);
        Instant expireDate = Nower.now().plus(Duration.ofDays(14));
        Instant tolerateUntil = expireDate.plus(Duration.ofDays(2));
        return new SignKey(keyId, system, new String(Hex.encode(secret)), expireDate, tolerateUntil);
    }
}
