-- 관리자 로그인 세션. 로그인 한 번이 한 행이고, 재발급할 때마다 토큰을 바꾸고(회전) 만료를 연장한다
-- 토큰 원문은 남기지 않고 SHA-256 해시만 저장한다. 만료된 행은 로그인할 때 지운다
CREATE TABLE refresh_token
(
    id                  BIGINT       NOT NULL,
    user_id             BIGINT       NOT NULL,
    token_hash          VARCHAR(64)  NOT NULL,
    previous_token_hash VARCHAR(64)  NULL,
    expires_at          TIMESTAMP(6) NOT NULL,
    created_at          TIMESTAMP(6) NOT NULL,
    updated_at          TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_token_token_hash UNIQUE (token_hash)
);

CREATE INDEX idx_refresh_token_previous_token_hash ON refresh_token (previous_token_hash);
CREATE INDEX idx_refresh_token_expires_at ON refresh_token (expires_at);

COMMENT ON TABLE refresh_token IS '관리자 리프레시 토큰';
COMMENT ON COLUMN refresh_token.id IS 'TSID';
COMMENT ON COLUMN refresh_token.user_id IS '사용자 ID';
COMMENT ON COLUMN refresh_token.token_hash IS '현재 토큰의 SHA-256 (hex)';
COMMENT ON COLUMN refresh_token.previous_token_hash IS '직전 토큰의 SHA-256 (hex). 다시 들어오면 재사용으로 본다';
COMMENT ON COLUMN refresh_token.expires_at IS '만료 시각. 재발급할 때마다 연장한다';
COMMENT ON COLUMN refresh_token.created_at IS '생성 시각';
COMMENT ON COLUMN refresh_token.updated_at IS '수정 시각';
