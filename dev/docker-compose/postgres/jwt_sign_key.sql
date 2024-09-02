CREATE TABLE jwt_sign_key (
    id               BIGINT      NOT NULL,
    secret           VARCHAR(64) NOT NULL,
    expire_date      TIMESTAMP   NOT NULL,
    tolerate_until   TIMESTAMP   NOT NULL,
    created_at       TIMESTAMP   NOT NULL,
    last_modified_at TIMESTAMP   NOT NULL,
    version          INT         NOT NULL,
    PRIMARY KEY (id)
);

COMMENT ON TABLE jwt_sign_key IS 'JWT 签发密钥表';
COMMENT ON COLUMN jwt_sign_key.id IS 'key id';
COMMENT ON COLUMN jwt_sign_key.secret IS 'key secret';
COMMENT ON COLUMN jwt_sign_key.expire_date IS 'key 过期时间。超过该时间不能使用该 key 签发 token';
COMMENT ON COLUMN jwt_sign_key.tolerate_until IS 'key 容忍时间。超过该时间不能使用该 key 验证 token';
COMMENT ON COLUMN jwt_sign_key.created_at IS '创建时间';
COMMENT ON COLUMN jwt_sign_key.last_modified_at IS '最后修改时间';
COMMENT ON COLUMN jwt_sign_key.version IS '版本号';
