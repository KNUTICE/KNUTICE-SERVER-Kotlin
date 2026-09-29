CREATE TABLE notice
(
    id                    BIGINT        NOT NULL COMMENT 'TSID',
    ntt_id                BIGINT        NOT NULL COMMENT '학교 게시판 게시글 번호 (API 식별자)',
    topic_code            INT           NOT NULL COMMENT '토픽 코드 (v2 topicId)',
    topic_name            VARCHAR(100)  NOT NULL COMMENT '토픽 이름 (v1 topic)',
    title                 VARCHAR(500)  NOT NULL COMMENT '제목',
    department            VARCHAR(100)  NOT NULL COMMENT '작성 부서',
    content_url           VARCHAR(1000) NOT NULL COMMENT '원문 URL',
    content_image_url     VARCHAR(1000) NULL COMMENT '본문 첫 이미지 (알림 이미지)',
    registration_date     DATE          NOT NULL COMMENT '게시일',
    is_attachment         BOOLEAN       NOT NULL COMMENT '첨부파일 여부',
    notification_status   VARCHAR(20)   NOT NULL COMMENT 'PENDING / SENT / SKIPPED',
    notified_at           DATETIME(6)   NULL COMMENT '알림 발송 완료 시각',
    summary_status        VARCHAR(20)   NOT NULL COMMENT 'PENDING / COMPLETED / FAILED / SKIPPED',
    summary_attempt_count INT           NOT NULL DEFAULT 0 COMMENT 'AI 요약 시도 횟수',
    created_at            DATETIME(6)   NOT NULL COMMENT '생성 시각',
    updated_at            DATETIME(6)   NOT NULL COMMENT '수정 시각',
    PRIMARY KEY (id),
    CONSTRAINT uk_notice_ntt_id UNIQUE (ntt_id),
    INDEX idx_notice_topic_code_ntt_id (topic_code, ntt_id),
    INDEX idx_notice_notification_status (notification_status),
    INDEX idx_notice_summary_status (summary_status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '공지';

CREATE TABLE notice_content
(
    id              BIGINT      NOT NULL COMMENT 'TSID',
    notice_id       BIGINT      NOT NULL COMMENT 'notice.id',
    content         MEDIUMTEXT  NULL COMMENT '본문 텍스트 (AI 요약 입력)',
    content_summary TEXT        NULL COMMENT 'AI 요약',
    created_at      DATETIME(6) NOT NULL COMMENT '생성 시각',
    updated_at      DATETIME(6) NOT NULL COMMENT '수정 시각',
    PRIMARY KEY (id),
    CONSTRAINT uk_notice_content_notice_id UNIQUE (notice_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '공지 본문 · AI 요약';

CREATE TABLE fcm_token
(
    id          BIGINT       NOT NULL COMMENT 'TSID',
    token       VARCHAR(512) NOT NULL COMMENT 'FCM 등록 토큰',
    device_type VARCHAR(20)  NOT NULL COMMENT 'iOS / AOS / UNKNOWN',
    is_active   BOOLEAN      NOT NULL COMMENT '발송 대상 여부',
    language    VARCHAR(10)  NOT NULL DEFAULT 'ko' COMMENT '알림 언어 (발송 시 해석)',
    created_at  DATETIME(6)  NOT NULL COMMENT '생성 시각',
    updated_at  DATETIME(6)  NOT NULL COMMENT '수정 시각',
    PRIMARY KEY (id),
    CONSTRAINT uk_fcm_token_token UNIQUE (token)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = 'FCM 등록 토큰';

CREATE TABLE fcm_token_subscription
(
    id           BIGINT       NOT NULL COMMENT 'TSID',
    fcm_token_id BIGINT       NOT NULL COMMENT 'FCM 토큰 ID',
    topic_code   INT          NOT NULL COMMENT '토픽 코드 (v2 topicId)',
    topic_name   VARCHAR(100) NOT NULL COMMENT '토픽 이름 (v1 topic)',
    created_at   DATETIME(6)  NOT NULL COMMENT '생성 시각',
    updated_at   DATETIME(6)  NOT NULL COMMENT '수정 시각',
    PRIMARY KEY (id),
    CONSTRAINT uk_fcm_token_subscription_topic_code_fcm_token_id UNIQUE (topic_code, fcm_token_id),
    INDEX idx_fcm_token_subscription_fcm_token_id (fcm_token_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = 'FCM 토큰의 토픽 구독';

CREATE TABLE seat_alert
(
    id           BIGINT      NOT NULL COMMENT 'TSID',
    fcm_token_id BIGINT      NOT NULL COMMENT 'FCM 토큰 ID',
    reading_room VARCHAR(20) NOT NULL COMMENT '열람실',
    seat_number  INT         NOT NULL COMMENT '좌석 번호',
    expires_at   DATETIME(6) NOT NULL COMMENT '만료 시각',
    created_at   DATETIME(6) NOT NULL COMMENT '생성 시각',
    updated_at   DATETIME(6) NOT NULL COMMENT '수정 시각',
    PRIMARY KEY (id),
    CONSTRAINT uk_seat_alert_fcm_token_id_reading_room_seat_number UNIQUE (fcm_token_id, reading_room, seat_number),
    INDEX idx_seat_alert_expires_at (expires_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '열람실 빈자리 알림';

CREATE TABLE users
(
    id         BIGINT       NOT NULL COMMENT 'TSID',
    email      VARCHAR(200) NOT NULL COMMENT '이메일 (로그인 ID)',
    password   VARCHAR(100) NOT NULL COMMENT '비밀번호 해시',
    nickname   VARCHAR(30)  NOT NULL COMMENT '닉네임',
    role       VARCHAR(20)  NOT NULL COMMENT 'ADMIN / USER',
    created_at DATETIME(6)  NOT NULL COMMENT '생성 시각',
    updated_at DATETIME(6)  NOT NULL COMMENT '수정 시각',
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_nickname UNIQUE (nickname)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '관리자 계정';

CREATE TABLE tip
(
    id          BIGINT        NOT NULL COMMENT 'TSID',
    title       VARCHAR(200)  NOT NULL COMMENT '제목',
    url         VARCHAR(1000) NOT NULL COMMENT '링크',
    device_type VARCHAR(20)   NOT NULL COMMENT 'iOS / AOS / UNKNOWN',
    created_at  DATETIME(6)   NOT NULL COMMENT '생성 시각',
    updated_at  DATETIME(6)   NOT NULL COMMENT '수정 시각',
    PRIMARY KEY (id),
    INDEX idx_tip_device_type_id (device_type, id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '사용 팁';

CREATE TABLE image
(
    id            BIGINT        NOT NULL COMMENT 'TSID',
    image_url     VARCHAR(1000) NOT NULL COMMENT '이미지 URL',
    original_name VARCHAR(255)  NOT NULL COMMENT '업로드 원본 파일명',
    server_name   VARCHAR(100)  NOT NULL COMMENT '서버 저장 파일명 (확장자 제외)',
    extension     VARCHAR(20)   NOT NULL COMMENT '확장자',
    image_type    VARCHAR(30)   NOT NULL COMMENT 'DEFAULT_IMAGE / TIP_IMAGE',
    created_at    DATETIME(6)   NOT NULL COMMENT '생성 시각',
    updated_at    DATETIME(6)   NOT NULL COMMENT '수정 시각',
    PRIMARY KEY (id),
    INDEX idx_image_image_type (image_type)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '이미지';

CREATE TABLE report
(
    id           BIGINT       NOT NULL COMMENT 'TSID',
    fcm_token_id BIGINT       NOT NULL COMMENT '문의한 FCM 토큰 ID',
    content      VARCHAR(500) NOT NULL COMMENT '문의 내용',
    device_name  VARCHAR(100) NOT NULL COMMENT '기기명',
    version      VARCHAR(50)  NOT NULL COMMENT '앱 버전',
    created_at   DATETIME(6)  NOT NULL COMMENT '생성 시각',
    updated_at   DATETIME(6)  NOT NULL COMMENT '수정 시각',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '앱 문의';
