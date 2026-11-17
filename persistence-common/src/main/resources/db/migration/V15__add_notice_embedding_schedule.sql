-- RAG 임베딩 Job 스케줄. 처음 채울 공지가 수천 건이고 무료 한도(하루 1,000회)를 나눠 쓰므로 10분마다 조금씩 이어서 한다
-- 꺼진 채로 넣는다. 임베딩 키의 한도 · 비용을 확인한 뒤 관리자 화면에서 켠다
INSERT INTO batch_schedule (id, schedule_key, job_name, job_parameters, cron, enabled, description, next_fire_at, last_fired_at, created_at, updated_at) VALUES
    (910029470997106138, 'notice-embedding', 'noticeEmbeddingJob', '{}', '0 */10 * * * *', FALSE, '공지 RAG 임베딩 (10분마다, 한도 · 비용 확인 후 켜기)', NULL, NULL, '2026-11-16 13:52:13', '2026-11-16 13:52:13');
