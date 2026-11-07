-- 앱 Open API 요청 로그 (일일 통계의 원본). 집계 날짜는 저장 시각이 아니라 requested_at 으로 정한다
-- 쿼리 문자열 · 토큰 원문 · IP 는 남기지 않는다. fcmToken 헤더는 토큰 ID 로 바꿔 저장한다
CREATE TABLE api_log
(
    id           BIGINT       NOT NULL,
    requested_at TIMESTAMP(6) NOT NULL,
    method       VARCHAR(10)  NOT NULL,
    url_pattern  VARCHAR(200) NOT NULL,
    status_code  INT          NOT NULL,
    duration_ms  INT          NOT NULL,
    topic_code   INT          NULL,
    topic_name   VARCHAR(100) NULL,
    fcm_token_id BIGINT       NULL,
    created_at   TIMESTAMP(6) NOT NULL,
    updated_at   TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id)
);

-- 목록은 요청 시각 · ID 내림차순이라 id 를 함께 둔다
CREATE INDEX idx_api_log_requested_at ON api_log (requested_at, id);

COMMENT ON TABLE api_log IS 'API 요청 로그';
COMMENT ON COLUMN api_log.id IS 'TSID';
COMMENT ON COLUMN api_log.requested_at IS '요청 시각 (KST)';
COMMENT ON COLUMN api_log.method IS 'HTTP 메서드';
COMMENT ON COLUMN api_log.url_pattern IS '요청이 매핑된 URL 패턴';
COMMENT ON COLUMN api_log.status_code IS '응답 상태 코드';
COMMENT ON COLUMN api_log.duration_ms IS '처리 시간 (밀리초)';
COMMENT ON COLUMN api_log.topic_code IS '공지 목록 조회의 토픽 코드 (v2 topicId)';
COMMENT ON COLUMN api_log.topic_name IS '공지 목록 조회의 토픽 이름 (v1 topic)';
COMMENT ON COLUMN api_log.fcm_token_id IS '요청한 기기의 FCM 토큰 ID';
COMMENT ON COLUMN api_log.created_at IS '생성 시각';
COMMENT ON COLUMN api_log.updated_at IS '수정 시각';
