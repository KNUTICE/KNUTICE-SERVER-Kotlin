-- 공지 질문(RAG) 기록. 질문 · 답변 · 근거 공지와 검색 설정을 남겨 답변 품질을 점검하고, 하루 질문 한도도 이 표로 센다
-- 질문에 이름 · 학번 같은 개인정보가 들어올 수 있으므로 오래 보관하지 않는다 (보관 기간 정리는 다음 작업)
-- 공지는 하드 삭제되므로 근거 공지는 외래 키 없이 nttId 목록으로 남긴다
CREATE TABLE notice_question
(
    id             BIGINT           NOT NULL,
    asked_by       VARCHAR(20)      NOT NULL,
    asker_key      VARCHAR(64)      NOT NULL,
    question       VARCHAR(500)     NOT NULL,
    status         VARCHAR(20)      NOT NULL,
    answer         TEXT             NULL,
    source_ntt_ids VARCHAR(500)     NULL,
    top_similarity DOUBLE PRECISION NULL,
    decay_per_year DOUBLE PRECISION NOT NULL,
    title_penalty  DOUBLE PRECISION NOT NULL,
    answer_model   VARCHAR(50)      NULL,
    input_tokens   INT              NULL,
    output_tokens  INT              NULL,
    elapsed_ms     INT              NOT NULL,
    verdict        VARCHAR(20)      NULL,
    memo           VARCHAR(500)     NULL,
    created_at     TIMESTAMP(6)     NOT NULL,
    updated_at     TIMESTAMP(6)     NOT NULL,
    PRIMARY KEY (id)
);

-- 하루 한도 세기 (`created_at >= 오늘 한도 시작`) · 최근 기록 조회
CREATE INDEX idx_notice_question_created_at ON notice_question (created_at);

COMMENT ON TABLE notice_question IS '공지 질문(RAG) 기록';
COMMENT ON COLUMN notice_question.id IS 'TSID';
COMMENT ON COLUMN notice_question.asked_by IS 'ADMIN';
COMMENT ON COLUMN notice_question.asker_key IS '질문한 관리자 user.id';
COMMENT ON COLUMN notice_question.question IS '질문';
COMMENT ON COLUMN notice_question.status IS 'ANSWERED / NOT_FOUND / SEARCH_ONLY';
COMMENT ON COLUMN notice_question.answer IS 'AI 답변. 근거가 약하거나 답변을 만들지 않았으면 NULL';
COMMENT ON COLUMN notice_question.source_ntt_ids IS '근거 공지 nttId (쉼표로 구분, 근거 번호 순서)';
COMMENT ON COLUMN notice_question.top_similarity IS '근거 공지 중 가장 높은 코사인 유사도';
COMMENT ON COLUMN notice_question.decay_per_year IS '검색 설정: 공지가 올라온 지 1년마다 빼는 점수';
COMMENT ON COLUMN notice_question.title_penalty IS '검색 설정: 제목만 있는 청크에서 빼는 점수';
COMMENT ON COLUMN notice_question.answer_model IS '답변 모델 (답변을 만들지 않았으면 NULL)';
COMMENT ON COLUMN notice_question.input_tokens IS '답변 입력 토큰';
COMMENT ON COLUMN notice_question.output_tokens IS '답변 출력 토큰';
COMMENT ON COLUMN notice_question.elapsed_ms IS '질문 처리에 걸린 시간 (ms)';
COMMENT ON COLUMN notice_question.verdict IS '관리자 판정 GOOD / BAD';
COMMENT ON COLUMN notice_question.memo IS '관리자 메모';
COMMENT ON COLUMN notice_question.created_at IS '생성 시각';
COMMENT ON COLUMN notice_question.updated_at IS '수정 시각';
