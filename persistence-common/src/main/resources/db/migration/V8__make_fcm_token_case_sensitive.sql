-- FCM 토큰은 대소문자를 구분하는 값이다. 테이블 기본 collation(_ai_ci)은 대소문자를 무시해
-- 대소문자만 다른 두 토큰을 같은 값으로 보고 유니크 제약 · 조회가 어긋나므로 이진 비교로 바꾼다
ALTER TABLE fcm_token
    MODIFY token VARCHAR(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin NOT NULL COMMENT 'FCM 등록 토큰 (대소문자 구분)';
