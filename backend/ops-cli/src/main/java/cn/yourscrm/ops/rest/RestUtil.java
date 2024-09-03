package cn.yourscrm.ops.rest;

import cn.yourscrm.ops.StateHolder;
import cn.yourscrm.ops.db.SimpleSignKey;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SimplePropertyRowMapper;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class RestUtil {
    public static @NotNull String genToken(StateHolder stateHolder) {
        SimpleSignKey key = stateHolder.getSimpleSignKey();
        if (key == null) {
            key = queryKey(stateHolder);
            stateHolder.setSimpleSignKey(key);
        }
        if (key.expireDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Sign key expired");
        }
        try {
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256).keyID(key.id() + "").build();
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject("ops")
                    .expirationTime(Date.from(Instant.now().plus(Duration.ofMinutes(30))))
                    .claim("usg", "ops")
                    .build();
            JWSObject jwsObject = new JWSObject(header, claimsSet.toPayload());
            MACSigner signer = new MACSigner(key.secret());
            jwsObject.sign(signer);
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to sign token", e);
        }
    }

    private static @NotNull SimpleSignKey queryKey(StateHolder stateHolder) {
        StateHolder.ConnectInfo connectInfo = stateHolder.getConnectInfo();
        if (connectInfo == null) {
            throw new IllegalStateException("Not connected");
        }
        var dsBuilder = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .driverClassName("org.postgresql.Driver")
                .url(String.format("jdbc:postgresql://%s:%d/%s",
                        connectInfo.getDbHost(), connectInfo.getDbPort(), connectInfo.getDbSchema()))
                .username(connectInfo.getDbUsername()).password(connectInfo.getDbPassword());
        SimpleSignKey key;
        try (var dataSource = dsBuilder.build()) {
            JdbcTemplate jdbcTpl = new JdbcTemplate(dataSource);
            // language=PostgreSQL
            String selectAllKeys = """
                    SELECT id, secret, expire_date, tolerate_until
                    FROM jwt_sign_key
                    ORDER BY created_at DESC""";
            List<SimpleSignKey> keys = jdbcTpl.query(selectAllKeys,
                    new SimplePropertyRowMapper<>(SimpleSignKey.class));
            Optional<SimpleSignKey> aKey = keys.stream()
                    .filter(k -> k.expireDate().isAfter(LocalDateTime.now())).findFirst();
            if (aKey.isEmpty()) {
                throw new IllegalStateException("No valid key found in the database");
            }
            return aKey.get();
        }
    }
}
