package cn.yourscrm.mono.auth.adaptor.jwt;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("jwt_sign_key")
public record SignKeyEntity(
        @Id long id,
        @NotNull String secret,
        @NotNull LocalDateTime expireDate,
        @NotNull LocalDateTime tolerateUntil,
        @NotNull LocalDateTime createdAt,
        @NotNull LocalDateTime lastModifiedAt,
        @Version int version
) {
}
