-- 토픽별 AI 요약 여부. 공지를 크롤링할 때 이 값으로 요약 대상(PENDING)인지 요약 안 함(SKIPPED)인지 정한다
-- 꺼 둔 동안 들어온 공지는 SKIPPED 로 확정되므로 다시 켜도 요약하지 않는다
ALTER TABLE topic
    ADD COLUMN summary_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'AI 요약 여부' AFTER visible;
