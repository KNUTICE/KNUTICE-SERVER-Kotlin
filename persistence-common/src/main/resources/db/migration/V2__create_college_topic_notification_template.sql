CREATE TABLE college
(
    id              BIGINT       NOT NULL,
    college_key     VARCHAR(50)  NOT NULL,
    display_name_ko VARCHAR(100) NOT NULL,
    display_name_en VARCHAR(100) NULL,
    display_name_ja VARCHAR(100) NULL,
    display_order   INT          NOT NULL,
    deleted_at      TIMESTAMP(6) NULL,
    created_at      TIMESTAMP(6) NOT NULL,
    updated_at      TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_college_college_key UNIQUE (college_key)
);

COMMENT ON TABLE college IS '단과대';
COMMENT ON COLUMN college.id IS 'TSID';
COMMENT ON COLUMN college.college_key IS '단과대 키 (생성 후 변경 불가)';
COMMENT ON COLUMN college.display_name_ko IS '표시명 (한국어)';
COMMENT ON COLUMN college.display_name_en IS '표시명 (영어)';
COMMENT ON COLUMN college.display_name_ja IS '표시명 (일본어)';
COMMENT ON COLUMN college.display_order IS '표시 순서';
COMMENT ON COLUMN college.deleted_at IS '삭제 시각';
COMMENT ON COLUMN college.created_at IS '생성 시각';
COMMENT ON COLUMN college.updated_at IS '수정 시각';

CREATE TABLE topic
(
    id              BIGINT       NOT NULL,
    code            INT          NOT NULL,
    name            VARCHAR(100) NOT NULL,
    topic_type      VARCHAR(20)  NOT NULL,
    display_name_ko VARCHAR(100) NOT NULL,
    display_name_en VARCHAR(100) NULL,
    display_name_ja VARCHAR(100) NULL,
    college_id      BIGINT       NULL,
    root_domain     VARCHAR(200) NOT NULL,
    bbs_path        VARCHAR(500) NOT NULL,
    crawl_enabled   BOOLEAN      NOT NULL,
    visible         BOOLEAN      NOT NULL,
    deleted_at      TIMESTAMP(6) NULL,
    created_at      TIMESTAMP(6) NOT NULL,
    updated_at      TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_topic_code UNIQUE (code),
    CONSTRAINT uk_topic_name UNIQUE (name)
);

CREATE INDEX idx_topic_college_id ON topic (college_id);

COMMENT ON TABLE topic IS '토픽 (공지 게시판 · 학과 게시판 · 학식)';
COMMENT ON COLUMN topic.id IS 'TSID';
COMMENT ON COLUMN topic.code IS '토픽 코드 (v2 topicId, 생성 후 변경 불가)';
COMMENT ON COLUMN topic.name IS '토픽 이름 (v1 topic, 생성 후 변경 불가)';
COMMENT ON COLUMN topic.topic_type IS 'NOTICE / MAJOR / MEAL';
COMMENT ON COLUMN topic.display_name_ko IS '표시명 (한국어)';
COMMENT ON COLUMN topic.display_name_en IS '표시명 (영어)';
COMMENT ON COLUMN topic.display_name_ja IS '표시명 (일본어)';
COMMENT ON COLUMN topic.college_id IS '단과대 ID (MAJOR 전용)';
COMMENT ON COLUMN topic.root_domain IS '크롤링 대상 도메인';
COMMENT ON COLUMN topic.bbs_path IS '크롤링 대상 게시판 경로';
COMMENT ON COLUMN topic.crawl_enabled IS '크롤링 대상 여부';
COMMENT ON COLUMN topic.visible IS '앱 노출 여부';
COMMENT ON COLUMN topic.deleted_at IS '삭제 시각';
COMMENT ON COLUMN topic.created_at IS '생성 시각';
COMMENT ON COLUMN topic.updated_at IS '수정 시각';

CREATE TABLE notification_template
(
    id           BIGINT       NOT NULL,
    template_key VARCHAR(50)  NOT NULL,
    text_ko      VARCHAR(500) NOT NULL,
    text_en      VARCHAR(500) NULL,
    text_ja      VARCHAR(500) NULL,
    description  VARCHAR(200) NOT NULL,
    created_at   TIMESTAMP(6) NOT NULL,
    updated_at   TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_notification_template_template_key UNIQUE (template_key)
);

COMMENT ON TABLE notification_template IS 'FCM 알림 문구';
COMMENT ON COLUMN notification_template.id IS 'TSID';
COMMENT ON COLUMN notification_template.template_key IS '템플릿 키 (코드의 NotificationTemplateKey)';
COMMENT ON COLUMN notification_template.text_ko IS '문구 (한국어)';
COMMENT ON COLUMN notification_template.text_en IS '문구 (영어)';
COMMENT ON COLUMN notification_template.text_ja IS '문구 (일본어)';
COMMENT ON COLUMN notification_template.description IS '관리자 화면용 설명';
COMMENT ON COLUMN notification_template.created_at IS '생성 시각';
COMMENT ON COLUMN notification_template.updated_at IS '수정 시각';
