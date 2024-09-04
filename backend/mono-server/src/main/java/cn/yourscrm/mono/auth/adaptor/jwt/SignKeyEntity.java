package cn.yourscrm.mono.auth.adaptor.jwt;

import cn.yourscrm.common.id.ArbitraryLongID;
import cn.yourscrm.mono.auth.domain.jwt.SignKey;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("jwt_sign_key")
public record SignKeyEntity(
        @Id long id,
        @NotNull String secret,
        @NotNull Instant expireDate,
        @NotNull Instant tolerateUntil,
        @CreatedDate
        @NotNull Instant createdAt,
        @LastModifiedDate
        @NotNull Instant lastModifiedAt,
        @Version int version
) {
    @NotNull
    public SignKey toDomain() {
        ArbitraryLongID system = new ArbitraryLongID(0);
        return new SignKey(new ArbitraryLongID(id), system, createdAt, system, lastModifiedAt, false,
                version, secret, expireDate, tolerateUntil);
    }

    @NotNull
    public static SignKeyEntity fromDomain(@NotNull SignKey domain) {
        return new SignKeyEntity(domain.getId().asLong(), domain.getSecret(), domain.getExpireDate(),
                domain.getTolerateUntil(), domain.getCreatedAt(), domain.getLastModifiedAt(),
                (int) domain.getVersion());
    }
}
