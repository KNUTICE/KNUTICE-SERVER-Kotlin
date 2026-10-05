-- 레거시 FcmNotificationAdapter · MessageFactory 에 하드코딩된 알림 문구 (ko). en · ja 는 NULL (조회 시 ko 로 대체)
-- placeholder 는 {이름} 형식이며 키별 목록은 코드의 NotificationTemplateKey 와 같아야 한다

INSERT INTO notification_template (id, template_key, text_ko, text_en, text_ja, description, created_at, updated_at) VALUES
    (892688444805480954, 'NOTICE_BODY_MULTIPLE', '{title} 외 {count}개의 소식이 있습니다.', NULL, NULL, '공지 3건 이상을 한 알림으로 보낼 때의 본문', '2026-09-29 17:30:00', '2026-09-29 17:30:00'),
    (892688444809675259, 'MEAL_HEADER', '{date} {mealName} 메뉴', NULL, NULL, '학식 알림 본문의 첫 줄', '2026-09-29 17:30:00', '2026-09-29 17:30:00'),
    (892688444809675260, 'MEAL_SECTION_KOREAN', '[한식]', NULL, NULL, '학식 알림의 한식 구분 제목', '2026-09-29 17:30:00', '2026-09-29 17:30:00'),
    (892688444809675261, 'MEAL_SECTION_TOP', '[일품]', NULL, NULL, '학식 알림의 일품 구분 제목', '2026-09-29 17:30:00', '2026-09-29 17:30:00'),
    (892688444809675262, 'MEAL_EMPTY', '등록된 식단 정보가 없습니다.', NULL, NULL, '등록된 식단이 없을 때의 학식 알림 본문', '2026-09-29 17:30:00', '2026-09-29 17:30:00'),
    (892688444809675263, 'SEAT_ALERT_TITLE', '빈자리 알림', NULL, NULL, '열람실 빈자리 알림 제목', '2026-09-29 17:30:00', '2026-09-29 17:30:00'),
    (892688444809675264, 'SEAT_ALERT_BODY', '{roomName} {seatNumber}번 좌석이 비었습니다!', NULL, NULL, '열람실 빈자리 알림 본문', '2026-09-29 17:30:00', '2026-09-29 17:30:00');
