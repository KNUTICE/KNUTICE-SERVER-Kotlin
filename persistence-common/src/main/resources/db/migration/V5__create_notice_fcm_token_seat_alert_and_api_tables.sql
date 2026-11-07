CREATE TABLE notice
(
    id                    BIGINT        NOT NULL,
    ntt_id                BIGINT        NOT NULL,
    topic_code            INT           NOT NULL,
    topic_name            VARCHAR(100)  NOT NULL,
    title                 VARCHAR(500)  NOT NULL,
    department            VARCHAR(100)  NOT NULL,
    content_url           VARCHAR(1000) NOT NULL,
    content_image_url     VARCHAR(1000) NULL,
    registration_date     DATE          NOT NULL,
    is_attachment         BOOLEAN       NOT NULL,
    notification_status   VARCHAR(20)   NOT NULL,
    notified_at           TIMESTAMP(6)  NULL,
    summary_status        VARCHAR(20)   NOT NULL,
    summary_attempt_count INT           NOT NULL DEFAULT 0,
    created_at            TIMESTAMP(6)  NOT NULL,
    updated_at            TIMESTAMP(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_notice_ntt_id UNIQUE (ntt_id)
);

CREATE INDEX idx_notice_topic_code_ntt_id ON notice (topic_code, ntt_id);
CREATE INDEX idx_notice_notification_status ON notice (notification_status);
CREATE INDEX idx_notice_summary_status ON notice (summary_status);

COMMENT ON TABLE notice IS '공지';
COMMENT ON COLUMN notice.id IS 'TSID';
COMMENT ON COLUMN notice.ntt_id IS '학교 게시판 게시글 번호 (API 식별자)';
COMMENT ON COLUMN notice.topic_code IS '토픽 코드 (v2 topicId)';
COMMENT ON COLUMN notice.topic_name IS '토픽 이름 (v1 topic)';
COMMENT ON COLUMN notice.title IS '제목';
COMMENT ON COLUMN notice.department IS '작성 부서';
COMMENT ON COLUMN notice.content_url IS '원문 URL';
COMMENT ON COLUMN notice.content_image_url IS '본문 첫 이미지 (알림 이미지)';
COMMENT ON COLUMN notice.registration_date IS '게시일';
COMMENT ON COLUMN notice.is_attachment IS '첨부파일 여부';
COMMENT ON COLUMN notice.notification_status IS 'PENDING / SENT / SKIPPED';
COMMENT ON COLUMN notice.notified_at IS '알림 발송 완료 시각';
COMMENT ON COLUMN notice.summary_status IS 'PENDING / COMPLETED / FAILED / SKIPPED';
COMMENT ON COLUMN notice.summary_attempt_count IS 'AI 요약 시도 횟수';
COMMENT ON COLUMN notice.created_at IS '생성 시각';
COMMENT ON COLUMN notice.updated_at IS '수정 시각';

CREATE TABLE notice_content
(
    id              BIGINT       NOT NULL,
    notice_id       BIGINT       NOT NULL,
    content         TEXT         NULL,
    content_summary TEXT         NULL,
    created_at      TIMESTAMP(6) NOT NULL,
    updated_at      TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_notice_content_notice_id UNIQUE (notice_id)
);

COMMENT ON TABLE notice_content IS '공지 본문 · AI 요약';
COMMENT ON COLUMN notice_content.id IS 'TSID';
COMMENT ON COLUMN notice_content.notice_id IS 'notice.id';
COMMENT ON COLUMN notice_content.content IS '본문 텍스트 (AI 요약 입력)';
COMMENT ON COLUMN notice_content.content_summary IS 'AI 요약';
COMMENT ON COLUMN notice_content.created_at IS '생성 시각';
COMMENT ON COLUMN notice_content.updated_at IS '수정 시각';

CREATE TABLE fcm_token
(
    id          BIGINT       NOT NULL,
    token       VARCHAR(512) NOT NULL,
    device_type VARCHAR(20)  NOT NULL,
    is_active   BOOLEAN      NOT NULL,
    language    VARCHAR(10)  NOT NULL DEFAULT 'ko',
    created_at  TIMESTAMP(6) NOT NULL,
    updated_at  TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_fcm_token_token UNIQUE (token)
);

COMMENT ON TABLE fcm_token IS 'FCM 등록 토큰';
COMMENT ON COLUMN fcm_token.id IS 'TSID';
COMMENT ON COLUMN fcm_token.token IS 'FCM 등록 토큰 (대소문자 구분)';
COMMENT ON COLUMN fcm_token.device_type IS 'iOS / AOS / UNKNOWN';
COMMENT ON COLUMN fcm_token.is_active IS '발송 대상 여부';
COMMENT ON COLUMN fcm_token.language IS '알림 언어 (발송 시 해석)';
COMMENT ON COLUMN fcm_token.created_at IS '생성 시각';
COMMENT ON COLUMN fcm_token.updated_at IS '수정 시각';

CREATE TABLE fcm_token_subscription
(
    id           BIGINT       NOT NULL,
    fcm_token_id BIGINT       NOT NULL,
    topic_code   INT          NOT NULL,
    topic_name   VARCHAR(100) NOT NULL,
    created_at   TIMESTAMP(6) NOT NULL,
    updated_at   TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_fcm_token_subscription_topic_code_fcm_token_id UNIQUE (topic_code, fcm_token_id)
);

CREATE INDEX idx_fcm_token_subscription_fcm_token_id ON fcm_token_subscription (fcm_token_id);

COMMENT ON TABLE fcm_token_subscription IS 'FCM 토큰의 토픽 구독';
COMMENT ON COLUMN fcm_token_subscription.id IS 'TSID';
COMMENT ON COLUMN fcm_token_subscription.fcm_token_id IS 'FCM 토큰 ID';
COMMENT ON COLUMN fcm_token_subscription.topic_code IS '토픽 코드 (v2 topicId)';
COMMENT ON COLUMN fcm_token_subscription.topic_name IS '토픽 이름 (v1 topic)';
COMMENT ON COLUMN fcm_token_subscription.created_at IS '생성 시각';
COMMENT ON COLUMN fcm_token_subscription.updated_at IS '수정 시각';

CREATE TABLE seat_alert
(
    id           BIGINT       NOT NULL,
    fcm_token_id BIGINT       NOT NULL,
    reading_room VARCHAR(20)  NOT NULL,
    seat_number  INT          NOT NULL,
    expires_at   TIMESTAMP(6) NOT NULL,
    created_at   TIMESTAMP(6) NOT NULL,
    updated_at   TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_seat_alert_fcm_token_id_reading_room_seat_number UNIQUE (fcm_token_id, reading_room, seat_number)
);

CREATE INDEX idx_seat_alert_expires_at ON seat_alert (expires_at);

COMMENT ON TABLE seat_alert IS '열람실 빈자리 알림';
COMMENT ON COLUMN seat_alert.id IS 'TSID';
COMMENT ON COLUMN seat_alert.fcm_token_id IS 'FCM 토큰 ID';
COMMENT ON COLUMN seat_alert.reading_room IS '열람실';
COMMENT ON COLUMN seat_alert.seat_number IS '좌석 번호';
COMMENT ON COLUMN seat_alert.expires_at IS '만료 시각';
COMMENT ON COLUMN seat_alert.created_at IS '생성 시각';
COMMENT ON COLUMN seat_alert.updated_at IS '수정 시각';

CREATE TABLE users
(
    id         BIGINT       NOT NULL,
    email      VARCHAR(200) NOT NULL,
    password   VARCHAR(100) NOT NULL,
    nickname   VARCHAR(30)  NOT NULL,
    role       VARCHAR(20)  NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id)
);

-- 이메일 · 닉네임은 대소문자를 무시하고 유일하다. 조회(IgnoreCase)가 UPPER 로 비교하므로 같은 식으로 인덱스를 만든다
CREATE UNIQUE INDEX uk_users_email ON users (UPPER(email));
CREATE UNIQUE INDEX uk_users_nickname ON users (UPPER(nickname));

COMMENT ON TABLE users IS '관리자 계정';
COMMENT ON COLUMN users.id IS 'TSID';
COMMENT ON COLUMN users.email IS '이메일 (로그인 ID, 대소문자 무시)';
COMMENT ON COLUMN users.password IS '비밀번호 해시';
COMMENT ON COLUMN users.nickname IS '닉네임 (대소문자 무시)';
COMMENT ON COLUMN users.role IS 'ADMIN / USER';
COMMENT ON COLUMN users.created_at IS '생성 시각';
COMMENT ON COLUMN users.updated_at IS '수정 시각';

CREATE TABLE tip
(
    id          BIGINT        NOT NULL,
    title       VARCHAR(200)  NOT NULL,
    url         VARCHAR(1000) NOT NULL,
    device_type VARCHAR(20)   NOT NULL,
    created_at  TIMESTAMP(6)  NOT NULL,
    updated_at  TIMESTAMP(6)  NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_tip_device_type_id ON tip (device_type, id);

COMMENT ON TABLE tip IS '사용 팁';
COMMENT ON COLUMN tip.id IS 'TSID';
COMMENT ON COLUMN tip.title IS '제목';
COMMENT ON COLUMN tip.url IS '링크';
COMMENT ON COLUMN tip.device_type IS 'iOS / AOS / UNKNOWN';
COMMENT ON COLUMN tip.created_at IS '생성 시각';
COMMENT ON COLUMN tip.updated_at IS '수정 시각';

CREATE TABLE image
(
    id            BIGINT        NOT NULL,
    image_url     VARCHAR(1000) NOT NULL,
    original_name VARCHAR(255)  NOT NULL,
    server_name   VARCHAR(100)  NOT NULL,
    extension     VARCHAR(20)   NOT NULL,
    image_type    VARCHAR(30)   NOT NULL,
    created_at    TIMESTAMP(6)  NOT NULL,
    updated_at    TIMESTAMP(6)  NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_image_image_type ON image (image_type);

COMMENT ON TABLE image IS '이미지';
COMMENT ON COLUMN image.id IS 'TSID';
COMMENT ON COLUMN image.image_url IS '이미지 URL';
COMMENT ON COLUMN image.original_name IS '업로드 원본 파일명';
COMMENT ON COLUMN image.server_name IS '서버 저장 파일명 (확장자 제외)';
COMMENT ON COLUMN image.extension IS '확장자';
COMMENT ON COLUMN image.image_type IS 'DEFAULT_IMAGE / TIP_IMAGE';
COMMENT ON COLUMN image.created_at IS '생성 시각';
COMMENT ON COLUMN image.updated_at IS '수정 시각';

CREATE TABLE report
(
    id           BIGINT       NOT NULL,
    fcm_token_id BIGINT       NOT NULL,
    content      VARCHAR(500) NOT NULL,
    device_name  VARCHAR(100) NOT NULL,
    version      VARCHAR(50)  NOT NULL,
    created_at   TIMESTAMP(6) NOT NULL,
    updated_at   TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id)
);

COMMENT ON TABLE report IS '앱 문의';
COMMENT ON COLUMN report.id IS 'TSID';
COMMENT ON COLUMN report.fcm_token_id IS '문의한 FCM 토큰 ID';
COMMENT ON COLUMN report.content IS '문의 내용';
COMMENT ON COLUMN report.device_name IS '기기명';
COMMENT ON COLUMN report.version IS '앱 버전';
COMMENT ON COLUMN report.created_at IS '생성 시각';
COMMENT ON COLUMN report.updated_at IS '수정 시각';
