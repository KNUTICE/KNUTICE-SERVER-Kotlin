CREATE TABLE batch_schedule
(
    id             BIGINT        NOT NULL,
    schedule_key   VARCHAR(100)  NOT NULL,
    job_name       VARCHAR(100)  NOT NULL,
    job_parameters VARCHAR(1000) NOT NULL,
    cron           VARCHAR(100)  NOT NULL,
    enabled        BOOLEAN       NOT NULL,
    description    VARCHAR(200)  NOT NULL,
    next_fire_at   TIMESTAMP(6)  NULL,
    last_fired_at  TIMESTAMP(6)  NULL,
    created_at     TIMESTAMP(6)  NOT NULL,
    updated_at     TIMESTAMP(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_batch_schedule_schedule_key UNIQUE (schedule_key)
);

COMMENT ON TABLE batch_schedule IS '배치 자동 실행 스케줄';
COMMENT ON COLUMN batch_schedule.id IS 'TSID';
COMMENT ON COLUMN batch_schedule.schedule_key IS '스케줄 키';
COMMENT ON COLUMN batch_schedule.job_name IS '실행할 Job 이름';
COMMENT ON COLUMN batch_schedule.job_parameters IS 'Job 파라미터 (JSON 객체)';
COMMENT ON COLUMN batch_schedule.cron IS 'Spring 6필드 cron (Asia/Seoul)';
COMMENT ON COLUMN batch_schedule.enabled IS '자동 실행 여부';
COMMENT ON COLUMN batch_schedule.description IS '관리자 화면용 설명';
COMMENT ON COLUMN batch_schedule.next_fire_at IS '다음 발화 시각';
COMMENT ON COLUMN batch_schedule.last_fired_at IS '마지막 발화 시각';
COMMENT ON COLUMN batch_schedule.created_at IS '생성 시각';
COMMENT ON COLUMN batch_schedule.updated_at IS '수정 시각';

CREATE TABLE batch_run_request
(
    id               BIGINT        NOT NULL,
    job_name         VARCHAR(100)  NOT NULL,
    job_parameters   VARCHAR(1000) NOT NULL,
    requested_by     VARCHAR(100)  NOT NULL,
    status           VARCHAR(20)   NOT NULL,
    job_execution_id BIGINT        NULL,
    processed_at     TIMESTAMP(6)  NULL,
    reject_reason    VARCHAR(500)  NULL,
    created_at       TIMESTAMP(6)  NOT NULL,
    updated_at       TIMESTAMP(6)  NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_batch_run_request_status_id ON batch_run_request (status, id);

COMMENT ON TABLE batch_run_request IS '배치 수동 실행 요청';
COMMENT ON COLUMN batch_run_request.id IS 'TSID';
COMMENT ON COLUMN batch_run_request.job_name IS '실행할 Job 이름';
COMMENT ON COLUMN batch_run_request.job_parameters IS 'Job 파라미터 (JSON 객체)';
COMMENT ON COLUMN batch_run_request.requested_by IS '요청한 관리자';
COMMENT ON COLUMN batch_run_request.status IS 'REQUESTED / LAUNCHED / REJECTED';
COMMENT ON COLUMN batch_run_request.job_execution_id IS '실행한 BATCH_JOB_EXECUTION ID';
COMMENT ON COLUMN batch_run_request.processed_at IS '실행 · 거절 시각';
COMMENT ON COLUMN batch_run_request.reject_reason IS '거절 사유';
COMMENT ON COLUMN batch_run_request.created_at IS '생성 시각';
COMMENT ON COLUMN batch_run_request.updated_at IS '수정 시각';

-- 레거시 @Scheduled cron 을 그대로 옮긴 초기 스케줄
-- next_fire_at 은 비워 둔다. 폴러가 처음 확인할 때 다음 발화 시각으로 채우므로 배포 직후 한꺼번에 실행되지 않는다
-- 메타데이터는 7일 보관한다 (열람실 빈자리 확인이 1분마다 실행 기록을 남긴다)
INSERT INTO batch_schedule (id, schedule_key, job_name, job_parameters, cron, enabled, description, next_fire_at, last_fired_at, created_at, updated_at) VALUES
    (892805280898322488, 'notice-crawl-notice', 'noticeCrawlJob', '{"topicType":"NOTICE"}', '0 0/15 * * * *', TRUE, '공지 게시판 크롤링 · 알림 · 요약 (15분마다)', NULL, NULL, '2026-09-30 01:09:26', '2026-09-30 01:09:26'),
    (892805280910905599, 'notice-crawl-major', 'noticeCrawlJob', '{"topicType":"MAJOR"}', '0 10 16 * * *', TRUE, '학과 게시판 크롤링 · 알림 · 요약 (매일 16:10)', NULL, NULL, '2026-09-30 01:09:26', '2026-09-30 01:09:26'),
    (892805280923488311, 'meal-notify', 'mealNotifyJob', '{}', '0 10 10 * * MON-FRI', TRUE, '학식 알림 (평일 10:10)', NULL, NULL, '2026-09-30 01:09:26', '2026-09-30 01:09:26'),
    (892805280936071423, 'silent-push', 'silentPushJob', '{}', '0 0 0 1 * *', TRUE, 'iOS 토큰 갱신용 사일런트 푸시 (매월 1일 00:00)', NULL, NULL, '2026-09-30 01:09:26', '2026-09-30 01:09:26'),
    (892805280948654323, 'seat-alert-check', 'seatAlertCheckJob', '{}', '0 * * * * *', TRUE, '열람실 빈자리 확인 · 알림, 만료 알림 정리 (1분마다)', NULL, NULL, '2026-09-30 01:09:26', '2026-09-30 01:09:26'),
    (892805280965431477, 'batch-maintenance', 'maintenanceJob', '{"retentionDays":"7"}', '0 30 4 * * *', TRUE, '7일이 지난 Spring Batch 메타데이터 삭제 (매일 04:30)', NULL, NULL, '2026-09-30 01:09:26', '2026-09-30 01:09:26');
