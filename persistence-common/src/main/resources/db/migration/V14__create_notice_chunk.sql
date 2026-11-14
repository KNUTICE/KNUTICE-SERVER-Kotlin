-- RAG(공지 질의응답)용 임베딩 저장소. 공지 하나를 제목 · 본문 조각 · 이미지 단위 청크로 나눠 벡터와 함께 저장한다
-- 임베딩 배치가 notice.embedding_status 로 대상(PENDING)을 찾고, 끝나면 그 공지의 청크를 통째로 바꿔 넣는다
-- vector 타입은 pgvector 확장이 만든다. 확장은 Flyway 밖에서 미리 만들어 둔다 (운영 DB · PostgreSqlContainerConfig)

-- 1. 공지별 임베딩 처리 상태. 기존 공지는 모두 PENDING 으로 시작해 배치가 차례로 채운다
--    모델을 바꾸면 해당 공지를 PENDING 으로 되돌려 다시 임베딩한다
ALTER TABLE notice
    ADD COLUMN embedding_status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN embedding_attempt_count INT         NOT NULL DEFAULT 0;

CREATE INDEX idx_notice_embedding_status ON notice (embedding_status);

COMMENT ON COLUMN notice.embedding_status IS 'PENDING / COMPLETED / FAILED';
COMMENT ON COLUMN notice.embedding_attempt_count IS '임베딩 시도 횟수';

-- 2. 청크. 공지마다 0 부터 순서대로 번호를 매긴다
--    - TITLE : 본문 · 이미지가 모두 없는 공지는 제목만으로 한 청크를 만든다
--    - BODY  : 본문을 나눈 조각. 검색 문맥을 위해 조각 앞에 제목을 붙인다
--    - IMAGE : 본문 이미지. 벡터는 이미지로 만들고, content 에는 답변 때 함께 보여 줄 제목을 둔다
-- 공지는 하드 삭제되므로 외래 키를 두지 않는다. 공지가 없어진 청크는 정리 Job 이 지운다 (이번 범위 밖)
-- 벡터 인덱스(HNSW)는 두지 않는다. 청크가 수천 ~ 수만 개라 전체 비교로 충분하고, 검색 속도를 재 본 뒤 필요하면 추가한다
CREATE TABLE notice_chunk
(
    id              BIGINT        NOT NULL,
    notice_id       BIGINT        NOT NULL,
    chunk_index     INT           NOT NULL,
    chunk_type      VARCHAR(20)   NOT NULL,
    content         TEXT          NOT NULL,
    image_url       VARCHAR(1000) NULL,
    embedding       vector(768)   NOT NULL,
    embedding_model VARCHAR(50)   NOT NULL,
    created_at      TIMESTAMP(6)  NOT NULL,
    updated_at      TIMESTAMP(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_notice_chunk_notice_id_chunk_index UNIQUE (notice_id, chunk_index)
);

COMMENT ON TABLE notice_chunk IS '공지 임베딩 청크 (RAG 검색 단위)';
COMMENT ON COLUMN notice_chunk.id IS 'TSID';
COMMENT ON COLUMN notice_chunk.notice_id IS 'notice.id';
COMMENT ON COLUMN notice_chunk.chunk_index IS '공지 안에서의 순서 (0부터)';
COMMENT ON COLUMN notice_chunk.chunk_type IS 'TITLE / BODY / IMAGE';
COMMENT ON COLUMN notice_chunk.content IS '임베딩한 텍스트 (BODY 는 제목 + 본문 조각, IMAGE 는 제목)';
COMMENT ON COLUMN notice_chunk.image_url IS 'IMAGE 청크의 원본 이미지 URL';
COMMENT ON COLUMN notice_chunk.embedding IS '임베딩 벡터 (768차원)';
COMMENT ON COLUMN notice_chunk.embedding_model IS '임베딩 모델 · 차원 (예: gemini-embedding-2@768). 모델을 바꾸면 다른 값끼리는 비교하지 않는다';
COMMENT ON COLUMN notice_chunk.created_at IS '생성 시각';
COMMENT ON COLUMN notice_chunk.updated_at IS '수정 시각';
