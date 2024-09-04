package cn.yourscrm.mono.auth.domain.jwt;

import cn.yourscrm.common.id.ID;
import cn.yourscrm.mono.ddd.domain.AggregateRoot;
import cn.yourscrm.mono.time.Nower;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
public class SignKey extends AggregateRoot {
    @NotNull
    private final String secret;
    @NotNull
    private final Instant expireDate;
    @NotNull
    private final Instant tolerateUntil;

    public SignKey(@NotNull ID id, @NotNull ID createdBy, @NotNull Instant createdAt,
                   @NotNull ID lastModifiedBy, @NotNull Instant lastModifiedAt, boolean deleted,
                   long version, @NotNull String secret, @NotNull Instant expireDate,
                   @NotNull Instant tolerateUntil) {
        super(id, createdBy, createdAt, lastModifiedBy, lastModifiedAt, deleted, version);
        this.secret = secret;
        this.expireDate = expireDate;
        this.tolerateUntil = tolerateUntil;
    }

    public SignKey(@NotNull ID id, @NotNull ID createdBy, @NotNull String secret, @NotNull Instant expireDate,
                   @NotNull Instant tolerateUntil) {
        super(id, createdBy);
        this.secret = secret;
        this.expireDate = expireDate;
        this.tolerateUntil = tolerateUntil;
    }

    public boolean canSignToken() {
        return expireDate.isAfter(Nower.now());
    }

    public boolean canVerifyToken() {
        return tolerateUntil.isAfter(Nower.now());
    }
}
