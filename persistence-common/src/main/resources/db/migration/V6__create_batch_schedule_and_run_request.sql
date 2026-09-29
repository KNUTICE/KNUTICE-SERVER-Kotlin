CREATE TABLE batch_schedule
(
    id             BIGINT        NOT NULL COMMENT 'TSID',
    schedule_key   VARCHAR(100)  NOT NULL COMMENT '스케줄 키',
    job_name       VARCHAR(100)  NOT NULL COMMENT '실행할 Job 이름',
    job_parameters VARCHAR(1000) NOT NULL COMMENT 'Job 파라미터 (JSON 객체)',
    cron           VARCHAR(100)  NOT NULL COMMENT 'Spring 6필드 cron (Asia/Seoul)',
    enabled        BOOLEAN       NOT NULL COMMENT '자동 실행 여부',
    description    VARCHAR(200)  NOT NULL COMMENT '관리자 화면용 설명',
    next_fire_at   DATETIME(6)   NULL COMMENT '다음 발화 시각',
    last_fired_at  DATETIME(6)   NULL COMMENT '마지막 발화 시각',
    created_at     DATETIME(6)   NOT NULL COMMENT '생성 시각',
    updated_at     DATETIME(6)   NOT NULL COMMENT '수정 시각',
    PRIMARY KEY (id),
    CONSTRAINT uk_batch_schedule_schedule_key UNIQUE (schedule_key)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '배치 자동 실행 스케줄';

CREATE TABLE batch_run_request
(
    id               BIGINT        NOT NULL COMMENT 'TSID',
    job_name         VARCHAR(100)  NOT NULL COMMENT '실행할 Job 이름',
    job_parameters   VARCHAR(1000) NOT NULL COMMENT 'Job 파라미터 (JSON 객체)',
    requested_by     VARCHAR(100)  NOT NULL COMMENT '요청한 관리자',
    status           VARCHAR(20)   NOT NULL COMMENT 'REQUESTED / LAUNCHED / REJECTED',
    job_execution_id BIGINT        NULL COMMENT '실행한 BATCH_JOB_EXECUTION ID',
    processed_at     DATETIME(6)   NULL COMMENT '실행 · 거절 시각',
    reject_reason    VARCHAR(500)  NULL COMMENT '거절 사유',
    created_at       DATETIME(6)   NOT NULL COMMENT '생성 시각',
    updated_at       DATETIME(6)   NOT NULL COMMENT '수정 시각',
    PRIMARY KEY (id),
    INDEX idx_batch_run_request_status_id (status, id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '배치 수동 실행 요청';

-- 레거시 @Scheduled cron 을 그대로 옮긴 초기 스케줄
-- next_fire_at 은 비워 둔다. 폴러가 처음 확인할 때 다음 발화 시각으로 채우므로 배포 직후 한꺼번에 실행되지 않는다
-- 메타데이터 정리는 보존 기간이 정해질 때까지 꺼 둔다
INSERT INTO batch_schedule (id, schedule_key, job_name, job_parameters, cron, enabled, description, next_fire_at, last_fired_at, created_at, updated_at) VALUES
    (892805280898322488, 'notice-crawl-notice', 'noticeCrawlJob', '{"topicType":"NOTICE"}', '0 0/15 * * * *', TRUE, '공지 게시판 크롤링 · 알림 · 요약 (15분마다)', NULL, NULL, '2026-09-30 01:09:26', '2026-09-30 01:09:26'),
    (892805280910905599, 'notice-crawl-major', 'noticeCrawlJob', '{"topicType":"MAJOR"}', '0 10 16 * * *', TRUE, '학과 게시판 크롤링 · 알림 · 요약 (매일 16:10)', NULL, NULL, '2026-09-30 01:09:26', '2026-09-30 01:09:26'),
    (892805280923488311, 'meal-notify', 'mealNotifyJob', '{}', '0 10 10 * * MON-FRI', TRUE, '학식 알림 (평일 10:10)', NULL, NULL, '2026-09-30 01:09:26', '2026-09-30 01:09:26'),
    (892805280936071423, 'silent-push', 'silentPushJob', '{}', '0 0 0 1 * *', TRUE, 'iOS 토큰 갱신용 사일런트 푸시 (매월 1일 00:00)', NULL, NULL, '2026-09-30 01:09:26', '2026-09-30 01:09:26'),
    (892805280948654323, 'seat-alert-check', 'seatAlertCheckJob', '{}', '0 * * * * *', TRUE, '열람실 빈자리 확인 · 알림, 만료 알림 정리 (1분마다)', NULL, NULL, '2026-09-30 01:09:26', '2026-09-30 01:09:26'),
    (892805280965431477, 'batch-maintenance', 'maintenanceJob', '{"retentionDays":"30"}', '0 30 4 * * *', FALSE, '보존 기간이 지난 Spring Batch 메타데이터 삭제 (매일 04:30)', NULL, NULL, '2026-09-30 01:09:26', '2026-09-30 01:09:26');
