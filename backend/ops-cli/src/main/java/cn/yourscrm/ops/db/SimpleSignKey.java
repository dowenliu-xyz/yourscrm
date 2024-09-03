package cn.yourscrm.ops.db;

import java.time.LocalDateTime;

public record SimpleSignKey(
        long id,
        String secret,
        LocalDateTime expireDate,
        LocalDateTime tolerateUntil
) {
}
