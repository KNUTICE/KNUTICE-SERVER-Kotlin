-- api_log 를 KST 날짜별로 집계한 통계. 집계 키마다 한 행이고, 다시 집계하면 값을 덮어쓴다
CREATE TABLE daily_api_log_statistics
(
    id                 BIGINT       NOT NULL COMMENT 'TSID',
    statistics_date    DATE         NOT NULL COMMENT '집계 날짜 (KST)',
    method             VARCHAR(10)  NOT NULL COMMENT 'HTTP 메서드',
    url_pattern        VARCHAR(200) NOT NULL COMMENT 'URL 패턴',
    request_count      BIGINT       NOT NULL COMMENT '요청 수',
    client_error_count BIGINT       NOT NULL COMMENT '4xx 응답 수',
    server_error_count BIGINT       NOT NULL COMMENT '5xx 응답 수',
    total_duration_ms  BIGINT       NOT NULL COMMENT '처리 시간 합 (평균 = 합 / 요청 수)',
    max_duration_ms    INT          NOT NULL COMMENT '최대 처리 시간 (밀리초)',
    device_count       INT          NOT NULL COMMENT '요청한 기기 수',
    created_at         DATETIME(6)  NOT NULL COMMENT '생성 시각',
    updated_at         DATETIME(6)  NOT NULL COMMENT '수정 시각',
    PRIMARY KEY (id),
    CONSTRAINT uk_daily_api_log_statistics_statistics_date_method_url_pattern UNIQUE (statistics_date, method, url_pattern)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '엔드포인트별 일일 API 통계';

CREATE TABLE daily_topic_statistics
(
    id              BIGINT       NOT NULL COMMENT 'TSID',
    statistics_date DATE         NOT NULL COMMENT '집계 날짜 (KST)',
    topic_code      INT          NOT NULL COMMENT '토픽 코드 (v2 topicId)',
    topic_name      VARCHAR(100) NOT NULL COMMENT '토픽 이름 (v1 topic)',
    request_count   BIGINT       NOT NULL COMMENT '공지 목록 조회 수 (v1 + v2)',
    created_at      DATETIME(6)  NOT NULL COMMENT '생성 시각',
    updated_at      DATETIME(6)  NOT NULL COMMENT '수정 시각',
    PRIMARY KEY (id),
    CONSTRAINT uk_daily_topic_statistics_statistics_date_topic_code UNIQUE (statistics_date, topic_code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '토픽별 일일 공지 목록 조회 통계';

-- 토큰 현황(스냅샷) 컬럼은 그날 마감 직후에만 구할 수 있어, 집계하지 못한 날짜는 NULL 로 남는다
CREATE TABLE daily_statistics
(
    id                       BIGINT      NOT NULL COMMENT 'TSID',
    statistics_date          DATE        NOT NULL COMMENT '집계 날짜 (KST)',
    request_count            BIGINT      NOT NULL COMMENT '앱 API 요청 수',
    device_count             INT         NOT NULL COMMENT '요청한 기기 수',
    ios_device_count         INT         NOT NULL COMMENT '요청한 iOS 기기 수',
    aos_device_count         INT         NOT NULL COMMENT '요청한 Android 기기 수',
    notice_count             INT         NOT NULL COMMENT '저장된 공지 수',
    summary_completed_count  INT         NOT NULL COMMENT '그날 공지 중 요약 완료 수 (집계 시점)',
    summary_failed_count     INT         NOT NULL COMMENT '그날 공지 중 요약 실패 수 (집계 시점)',
    new_token_count          INT         NOT NULL COMMENT '등록된 FCM 토큰 수',
    active_ios_token_count   INT         NULL COMMENT '활성 iOS 토큰 수 (그날 마감 직후, NULL = 미수집)',
    active_aos_token_count   INT         NULL COMMENT '활성 Android 토큰 수 (그날 마감 직후, NULL = 미수집)',
    inactive_ios_token_count INT         NULL COMMENT '비활성 iOS 토큰 수 (그날 마감 직후, NULL = 미수집)',
    inactive_aos_token_count INT         NULL COMMENT '비활성 Android 토큰 수 (그날 마감 직후, NULL = 미수집)',
    created_at               DATETIME(6) NOT NULL COMMENT '생성 시각',
    updated_at               DATETIME(6) NOT NULL COMMENT '수정 시각',
    PRIMARY KEY (id),
    CONSTRAINT uk_daily_statistics_statistics_date UNIQUE (statistics_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '일일 API · 서비스 현황 통계';

-- 매일 00:10 에 최근 3일을 다시 집계하고 30일이 지난 API 로그를 지운다
-- next_fire_at 은 비워 둔다. 폴러가 처음 확인할 때 다음 발화 시각으로 채운다
INSERT INTO batch_schedule (id, schedule_key, job_name, job_parameters, cron, enabled, description, next_fire_at, last_fired_at, created_at, updated_at) VALUES
    (897704515545528220, 'daily-statistics', 'dailyStatisticsJob', '{"lookbackDays":"3","apiLogRetentionDays":"30"}', '0 10 0 * * *', TRUE, '일일 통계 집계 · 30일 지난 API 로그 삭제 (매일 00:10)', NULL, NULL, '2026-10-13 13:37:15', '2026-10-13 13:37:15');
