-- 앱 Open API 요청 로그 (일일 통계의 원본). 집계 날짜는 저장 시각이 아니라 requested_at 으로 정한다
-- 쿼리 문자열 · 토큰 원문 · IP 는 남기지 않는다. fcmToken 헤더는 토큰 ID 로 바꿔 저장한다
CREATE TABLE api_log
(
    id           BIGINT       NOT NULL COMMENT 'TSID',
    requested_at DATETIME(6)  NOT NULL COMMENT '요청 시각 (KST)',
    method       VARCHAR(10)  NOT NULL COMMENT 'HTTP 메서드',
    url_pattern  VARCHAR(200) NOT NULL COMMENT '요청이 매핑된 URL 패턴',
    status_code  INT          NOT NULL COMMENT '응답 상태 코드',
    duration_ms  INT          NOT NULL COMMENT '처리 시간 (밀리초)',
    topic_code   INT          NULL COMMENT '공지 목록 조회의 토픽 코드 (v2 topicId)',
    topic_name   VARCHAR(100) NULL COMMENT '공지 목록 조회의 토픽 이름 (v1 topic)',
    fcm_token_id BIGINT       NULL COMMENT '요청한 기기의 FCM 토큰 ID',
    created_at   DATETIME(6)  NOT NULL COMMENT '생성 시각',
    updated_at   DATETIME(6)  NOT NULL COMMENT '수정 시각',
    PRIMARY KEY (id),
    INDEX idx_api_log_requested_at (requested_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = 'API 요청 로그';
