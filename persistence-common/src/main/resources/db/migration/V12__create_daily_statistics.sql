-- api_log 를 KST 날짜별로 집계한 통계. 집계 키마다 한 행이고, 다시 집계하면 값을 덮어쓴다
CREATE TABLE daily_api_log_statistics
(
    id                 BIGINT       NOT NULL,
    statistics_date    DATE         NOT NULL,
    method             VARCHAR(10)  NOT NULL,
    url_pattern        VARCHAR(200) NOT NULL,
    request_count      BIGINT       NOT NULL,
    client_error_count BIGINT       NOT NULL,
    server_error_count BIGINT       NOT NULL,
    total_duration_ms  BIGINT       NOT NULL,
    max_duration_ms    INT          NOT NULL,
    device_count       INT          NOT NULL,
    created_at         TIMESTAMP(6) NOT NULL,
    updated_at         TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_daily_api_log_statistics_statistics_date_method_url_pattern UNIQUE (statistics_date, method, url_pattern)
);

COMMENT ON TABLE daily_api_log_statistics IS '엔드포인트별 일일 API 통계';
COMMENT ON COLUMN daily_api_log_statistics.id IS 'TSID';
COMMENT ON COLUMN daily_api_log_statistics.statistics_date IS '집계 날짜 (KST)';
COMMENT ON COLUMN daily_api_log_statistics.method IS 'HTTP 메서드';
COMMENT ON COLUMN daily_api_log_statistics.url_pattern IS 'URL 패턴';
COMMENT ON COLUMN daily_api_log_statistics.request_count IS '요청 수';
COMMENT ON COLUMN daily_api_log_statistics.client_error_count IS '4xx 응답 수';
COMMENT ON COLUMN daily_api_log_statistics.server_error_count IS '5xx 응답 수';
COMMENT ON COLUMN daily_api_log_statistics.total_duration_ms IS '처리 시간 합 (평균 = 합 / 요청 수)';
COMMENT ON COLUMN daily_api_log_statistics.max_duration_ms IS '최대 처리 시간 (밀리초)';
COMMENT ON COLUMN daily_api_log_statistics.device_count IS '요청한 기기 수';
COMMENT ON COLUMN daily_api_log_statistics.created_at IS '생성 시각';
COMMENT ON COLUMN daily_api_log_statistics.updated_at IS '수정 시각';

CREATE TABLE daily_topic_statistics
(
    id              BIGINT       NOT NULL,
    statistics_date DATE         NOT NULL,
    topic_code      INT          NOT NULL,
    topic_name      VARCHAR(100) NOT NULL,
    request_count   BIGINT       NOT NULL,
    created_at      TIMESTAMP(6) NOT NULL,
    updated_at      TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_daily_topic_statistics_statistics_date_topic_code UNIQUE (statistics_date, topic_code)
);

COMMENT ON TABLE daily_topic_statistics IS '토픽별 일일 공지 목록 조회 통계';
COMMENT ON COLUMN daily_topic_statistics.id IS 'TSID';
COMMENT ON COLUMN daily_topic_statistics.statistics_date IS '집계 날짜 (KST)';
COMMENT ON COLUMN daily_topic_statistics.topic_code IS '토픽 코드 (v2 topicId)';
COMMENT ON COLUMN daily_topic_statistics.topic_name IS '토픽 이름 (v1 topic)';
COMMENT ON COLUMN daily_topic_statistics.request_count IS '공지 목록 조회 수 (v1 + v2)';
COMMENT ON COLUMN daily_topic_statistics.created_at IS '생성 시각';
COMMENT ON COLUMN daily_topic_statistics.updated_at IS '수정 시각';

-- 토큰 현황(스냅샷) 컬럼은 그날 마감 직후에만 구할 수 있어, 집계하지 못한 날짜는 NULL 로 남는다
CREATE TABLE daily_statistics
(
    id                       BIGINT       NOT NULL,
    statistics_date          DATE         NOT NULL,
    request_count            BIGINT       NOT NULL,
    device_count             INT          NOT NULL,
    ios_device_count         INT          NOT NULL,
    aos_device_count         INT          NOT NULL,
    notice_count             INT          NOT NULL,
    summary_completed_count  INT          NOT NULL,
    summary_failed_count     INT          NOT NULL,
    new_token_count          INT          NOT NULL,
    active_ios_token_count   INT          NULL,
    active_aos_token_count   INT          NULL,
    inactive_ios_token_count INT          NULL,
    inactive_aos_token_count INT          NULL,
    created_at               TIMESTAMP(6) NOT NULL,
    updated_at               TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_daily_statistics_statistics_date UNIQUE (statistics_date)
);

COMMENT ON TABLE daily_statistics IS '일일 API · 서비스 현황 통계';
COMMENT ON COLUMN daily_statistics.id IS 'TSID';
COMMENT ON COLUMN daily_statistics.statistics_date IS '집계 날짜 (KST)';
COMMENT ON COLUMN daily_statistics.request_count IS '앱 API 요청 수';
COMMENT ON COLUMN daily_statistics.device_count IS '요청한 기기 수';
COMMENT ON COLUMN daily_statistics.ios_device_count IS '요청한 iOS 기기 수';
COMMENT ON COLUMN daily_statistics.aos_device_count IS '요청한 Android 기기 수';
COMMENT ON COLUMN daily_statistics.notice_count IS '저장된 공지 수';
COMMENT ON COLUMN daily_statistics.summary_completed_count IS '그날 공지 중 요약 완료 수 (집계 시점)';
COMMENT ON COLUMN daily_statistics.summary_failed_count IS '그날 공지 중 요약 실패 수 (집계 시점)';
COMMENT ON COLUMN daily_statistics.new_token_count IS '등록된 FCM 토큰 수';
COMMENT ON COLUMN daily_statistics.active_ios_token_count IS '활성 iOS 토큰 수 (그날 마감 직후, NULL = 미수집)';
COMMENT ON COLUMN daily_statistics.active_aos_token_count IS '활성 Android 토큰 수 (그날 마감 직후, NULL = 미수집)';
COMMENT ON COLUMN daily_statistics.inactive_ios_token_count IS '비활성 iOS 토큰 수 (그날 마감 직후, NULL = 미수집)';
COMMENT ON COLUMN daily_statistics.inactive_aos_token_count IS '비활성 Android 토큰 수 (그날 마감 직후, NULL = 미수집)';
COMMENT ON COLUMN daily_statistics.created_at IS '생성 시각';
COMMENT ON COLUMN daily_statistics.updated_at IS '수정 시각';

-- 매일 00:10 에 최근 3일을 다시 집계하고 30일이 지난 API 로그를 지운다
-- next_fire_at 은 비워 둔다. 폴러가 처음 확인할 때 다음 발화 시각으로 채운다
INSERT INTO batch_schedule (id, schedule_key, job_name, job_parameters, cron, enabled, description, next_fire_at, last_fired_at, created_at, updated_at) VALUES
    (897704515545528220, 'daily-statistics', 'dailyStatisticsJob', '{"lookbackDays":"3","apiLogRetentionDays":"30"}', '0 10 0 * * *', TRUE, '일일 통계 집계 · 30일 지난 API 로그 삭제 (매일 00:10)', NULL, NULL, '2026-10-13 13:37:15', '2026-10-13 13:37:15');
