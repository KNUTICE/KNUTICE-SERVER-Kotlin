-- AI 요약을 크롤링 Job 에서 떼어 내 별도 Job 으로 실행한다
-- 요약이 길어져도 크롤링 · 발송이 밀리지 않고, 이 스케줄의 enabled 로 요약만 끄고 켤 수 있다
INSERT INTO batch_schedule (id, schedule_key, job_name, job_parameters, cron, enabled, description, next_fire_at, last_fired_at, created_at, updated_at) VALUES
    (893497211373609235, 'notice-summary', 'noticeSummaryJob', '{}', '0 */5 * * * *', TRUE, '공지 AI 요약 (5분마다)', NULL, NULL, '2026-10-01 22:58:55', '2026-10-01 22:58:55');

UPDATE batch_schedule SET description = '공지 게시판 크롤링 · 알림 (15분마다)', updated_at = '2026-10-01 22:58:55' WHERE schedule_key = 'notice-crawl-notice';
UPDATE batch_schedule SET description = '학과 게시판 크롤링 · 알림 (매일 16:10)', updated_at = '2026-10-01 22:58:55' WHERE schedule_key = 'notice-crawl-major';
