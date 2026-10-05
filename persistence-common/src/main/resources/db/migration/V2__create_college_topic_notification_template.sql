CREATE TABLE college
(
    id              BIGINT       NOT NULL COMMENT 'TSID',
    college_key     VARCHAR(50)  NOT NULL COMMENT '단과대 키 (생성 후 변경 불가)',
    display_name_ko VARCHAR(100) NOT NULL COMMENT '표시명 (한국어)',
    display_name_en VARCHAR(100) NULL COMMENT '표시명 (영어)',
    display_name_ja VARCHAR(100) NULL COMMENT '표시명 (일본어)',
    display_order   INT          NOT NULL COMMENT '표시 순서',
    deleted_at      DATETIME(6)  NULL COMMENT '삭제 시각',
    created_at      DATETIME(6)  NOT NULL COMMENT '생성 시각',
    updated_at      DATETIME(6)  NOT NULL COMMENT '수정 시각',
    PRIMARY KEY (id),
    CONSTRAINT uk_college_college_key UNIQUE (college_key)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '단과대';

CREATE TABLE topic
(
    id              BIGINT       NOT NULL COMMENT 'TSID',
    code            INT          NOT NULL COMMENT '토픽 코드 (v2 topicId, 생성 후 변경 불가)',
    name            VARCHAR(100) NOT NULL COMMENT '토픽 이름 (v1 topic, 생성 후 변경 불가)',
    topic_type      VARCHAR(20)  NOT NULL COMMENT 'NOTICE / MAJOR / MEAL',
    display_name_ko VARCHAR(100) NOT NULL COMMENT '표시명 (한국어)',
    display_name_en VARCHAR(100) NULL COMMENT '표시명 (영어)',
    display_name_ja VARCHAR(100) NULL COMMENT '표시명 (일본어)',
    college_id      BIGINT       NULL COMMENT '단과대 ID (MAJOR 전용)',
    root_domain     VARCHAR(200) NOT NULL COMMENT '크롤링 대상 도메인',
    bbs_path        VARCHAR(500) NOT NULL COMMENT '크롤링 대상 게시판 경로',
    crawl_enabled   BOOLEAN      NOT NULL COMMENT '크롤링 대상 여부',
    visible         BOOLEAN      NOT NULL COMMENT '앱 노출 여부',
    deleted_at      DATETIME(6)  NULL COMMENT '삭제 시각',
    created_at      DATETIME(6)  NOT NULL COMMENT '생성 시각',
    updated_at      DATETIME(6)  NOT NULL COMMENT '수정 시각',
    PRIMARY KEY (id),
    CONSTRAINT uk_topic_code UNIQUE (code),
    CONSTRAINT uk_topic_name UNIQUE (name),
    INDEX idx_topic_college_id (college_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '토픽 (공지 게시판 · 학과 게시판 · 학식)';

CREATE TABLE notification_template
(
    id           BIGINT       NOT NULL COMMENT 'TSID',
    template_key VARCHAR(50)  NOT NULL COMMENT '템플릿 키 (코드의 NotificationTemplateKey)',
    text_ko      VARCHAR(500) NOT NULL COMMENT '문구 (한국어)',
    text_en      VARCHAR(500) NULL COMMENT '문구 (영어)',
    text_ja      VARCHAR(500) NULL COMMENT '문구 (일본어)',
    description  VARCHAR(200) NOT NULL COMMENT '관리자 화면용 설명',
    created_at   DATETIME(6)  NOT NULL COMMENT '생성 시각',
    updated_at   DATETIME(6)  NOT NULL COMMENT '수정 시각',
    PRIMARY KEY (id),
    CONSTRAINT uk_notification_template_template_key UNIQUE (template_key)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = 'FCM 알림 문구';
