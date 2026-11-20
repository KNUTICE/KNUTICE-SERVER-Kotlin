-- 공지 질문(RAG) 기록. 질문 · 근거 공지 · 답변과 검색 설정을 남겨 답변 품질을 점검하고, 하루 질문 한도도 이 표로 센다
-- 답변은 서버가 만들지 않는다. 서버는 검색 · 프롬프트만 만들고(PREPARED), 앱 · 관리자 웹이 사용자 개인 AI 키로 답변을 받아 돌려주면 저장한다(ANSWERED)
-- 질문에 이름 · 학번 같은 개인정보가 들어올 수 있으므로 오래 보관하지 않는다 (보관 기간 정리는 다음 작업)
-- 기기 토큰(fcmToken) 원문은 저장하지 않고 HMAC 해시만 남긴다
-- 공지는 하드 삭제되므로 근거 공지는 외래 키 없이 nttId 목록으로 남긴다
CREATE TABLE notice_question
(
    id              BIGINT           NOT NULL,
    asked_by        VARCHAR(20)      NOT NULL,
    asker_key       VARCHAR(64)      NOT NULL,
    question        VARCHAR(500)     NOT NULL,
    status          VARCHAR(20)      NOT NULL,
    source_ntt_ids  VARCHAR(500)     NULL,
    top_similarity  DOUBLE PRECISION NULL,
    decay_per_year  DOUBLE PRECISION NOT NULL,
    title_penalty   DOUBLE PRECISION NOT NULL,
    elapsed_ms      INT              NOT NULL,
    answer          TEXT             NULL,
    answer_provider VARCHAR(20)      NULL,
    answer_model    VARCHAR(50)      NULL,
    input_tokens    INT              NULL,
    output_tokens   INT              NULL,
    answered_at     TIMESTAMP(6)     NULL,
    verdict         VARCHAR(20)      NULL,
    memo            VARCHAR(500)     NULL,
    created_at      TIMESTAMP(6)     NOT NULL,
    updated_at      TIMESTAMP(6)     NOT NULL,
    PRIMARY KEY (id)
);

-- 하루 한도 세기 (`created_at >= 오늘 한도 시작`) · 최근 기록 조회
CREATE INDEX idx_notice_question_created_at ON notice_question (created_at);
-- 기기 · 관리자별 하루 한도 세기
CREATE INDEX idx_notice_question_asker_key_created_at ON notice_question (asker_key, created_at);

COMMENT ON TABLE notice_question IS '공지 질문(RAG) 기록';
COMMENT ON COLUMN notice_question.id IS 'TSID';
COMMENT ON COLUMN notice_question.asked_by IS 'ADMIN / APP';
COMMENT ON COLUMN notice_question.asker_key IS 'ADMIN 은 user.id, APP 은 fcmToken 의 HMAC-SHA256 (원문 저장 안 함)';
COMMENT ON COLUMN notice_question.question IS '질문';
COMMENT ON COLUMN notice_question.status IS 'PREPARED (프롬프트까지) / ANSWERED (답변 저장) / NOT_FOUND (근거 공지 없음)';
COMMENT ON COLUMN notice_question.source_ntt_ids IS '근거 공지 nttId (쉼표로 구분, 근거 번호 순서)';
COMMENT ON COLUMN notice_question.top_similarity IS '근거 공지 중 가장 높은 코사인 유사도';
COMMENT ON COLUMN notice_question.decay_per_year IS '검색 설정: 공지가 올라온 지 1년마다 빼는 점수';
COMMENT ON COLUMN notice_question.title_penalty IS '검색 설정: 제목만 있는 청크에서 빼는 점수';
COMMENT ON COLUMN notice_question.elapsed_ms IS '검색 · 프롬프트 준비에 걸린 시간 (ms)';
COMMENT ON COLUMN notice_question.answer IS '사용자 키로 받은 답변 (없는 근거 번호는 지운 것)';
COMMENT ON COLUMN notice_question.answer_provider IS 'FACTCHAT / GEMINI';
COMMENT ON COLUMN notice_question.answer_model IS '답변 모델';
COMMENT ON COLUMN notice_question.input_tokens IS '답변 입력 토큰 (앱이 알려 준 값)';
COMMENT ON COLUMN notice_question.output_tokens IS '답변 출력 토큰 (앱이 알려 준 값)';
COMMENT ON COLUMN notice_question.answered_at IS '답변 저장 시각';
COMMENT ON COLUMN notice_question.verdict IS '판정 GOOD / BAD';
COMMENT ON COLUMN notice_question.memo IS '판정 메모';
COMMENT ON COLUMN notice_question.created_at IS '생성 시각';
COMMENT ON COLUMN notice_question.updated_at IS '수정 시각';
