package cn.yourscrm.mono.auth.domain.jwt;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SignKey(
        long id,
        @NotNull String secret,
        @NotNull LocalDateTime expireDate,
        @NotNull LocalDateTime tolerateUntil,
        int version
) {
    public boolean canSignToken() {
        return expireDate.isAfter(LocalDateTime.now());
    }

    public boolean canVerifyToken() {
        return tolerateUntil.isAfter(LocalDateTime.now());
    }
}
