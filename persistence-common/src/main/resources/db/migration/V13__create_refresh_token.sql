-- 관리자 로그인 세션. 로그인 한 번이 한 행이고, 재발급할 때마다 토큰을 바꾸고(회전) 만료를 연장한다
-- 토큰 원문은 남기지 않고 SHA-256 해시만 저장한다. 만료된 행은 로그인할 때 지운다
CREATE TABLE refresh_token
(
    id                  BIGINT      NOT NULL COMMENT 'TSID',
    user_id             BIGINT      NOT NULL COMMENT '사용자 ID',
    token_hash          VARCHAR(64) NOT NULL COMMENT '현재 토큰의 SHA-256 (hex)',
    previous_token_hash VARCHAR(64) NULL COMMENT '직전 토큰의 SHA-256 (hex). 다시 들어오면 재사용으로 본다',
    expires_at          DATETIME(6) NOT NULL COMMENT '만료 시각. 재발급할 때마다 연장한다',
    created_at          DATETIME(6) NOT NULL COMMENT '생성 시각',
    updated_at          DATETIME(6) NOT NULL COMMENT '수정 시각',
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_token_token_hash UNIQUE (token_hash),
    INDEX idx_refresh_token_previous_token_hash (previous_token_hash),
    INDEX idx_refresh_token_expires_at (expires_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '관리자 리프레시 토큰';
