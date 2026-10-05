-- 알림 문구 · 토픽 표시명의 영어 · 일본어 번역
-- 토픽 표시명은 앱 문자열 리소스와 같게 맞춘다. 알림 문구는 앱에 없는 문구라 새로 번역했다
-- 알림 문구의 placeholder 는 키별 목록(NotificationTemplateKey)과 같아야 한다

UPDATE notification_template SET text_en = '{title} and {count} more updates.', text_ja = '{title} ほか{count}件のお知らせがあります。', updated_at = '2026-10-05 19:10:12' WHERE template_key = 'NOTICE_BODY_MULTIPLE';
UPDATE notification_template SET text_en = '{mealName} menu for {date}', text_ja = '{date} {mealName}のメニュー', updated_at = '2026-10-05 19:10:12' WHERE template_key = 'MEAL_HEADER';
UPDATE notification_template SET text_en = '[Korean Meal]', text_ja = '[韓国料理]', updated_at = '2026-10-05 19:10:12' WHERE template_key = 'MEAL_SECTION_KOREAN';
UPDATE notification_template SET text_en = '[Special Dish]', text_ja = '[一品料理]', updated_at = '2026-10-05 19:10:12' WHERE template_key = 'MEAL_SECTION_TOP';
UPDATE notification_template SET text_en = 'No menu has been posted.', text_ja = '登録されたメニューはありません。', updated_at = '2026-10-05 19:10:12' WHERE template_key = 'MEAL_EMPTY';
UPDATE notification_template SET text_en = 'Seat Available', text_ja = '空席のお知らせ', updated_at = '2026-10-05 19:10:12' WHERE template_key = 'SEAT_ALERT_TITLE';
UPDATE notification_template SET text_en = 'Seat {seatNumber} in {roomName} is now available!', text_ja = '{roomName}の{seatNumber}番席が空きました！', updated_at = '2026-10-05 19:10:12' WHERE template_key = 'SEAT_ALERT_BODY';

-- 공지 · 학식
UPDATE topic SET display_name_en = 'General', display_name_ja = 'お知らせ', updated_at = '2026-10-05 19:10:12' WHERE code = 1;
UPDATE topic SET display_name_en = 'Scholarship', display_name_ja = '奨学', updated_at = '2026-10-05 19:10:12' WHERE code = 2;
UPDATE topic SET display_name_en = 'Event', display_name_ja = 'イベント', updated_at = '2026-10-05 19:10:12' WHERE code = 3;
UPDATE topic SET display_name_en = 'Academic', display_name_ja = '学事', updated_at = '2026-10-05 19:10:12' WHERE code = 4;
UPDATE topic SET display_name_en = 'Career News', display_name_ja = '就職', updated_at = '2026-10-05 19:10:12' WHERE code = 5;
UPDATE topic SET display_name_en = 'Student Dining Hall', display_name_ja = '学生食堂', updated_at = '2026-10-05 19:10:12' WHERE code = 900;
UPDATE topic SET display_name_en = 'Staff Dining Hall', display_name_ja = '教職員食堂', updated_at = '2026-10-05 19:10:12' WHERE code = 901;

-- 폐지 학과 (구버전 앱 호환용)
UPDATE topic SET display_name_en = 'AI & Robotics Engineering', display_name_ja = 'AIロボット工学科', updated_at = '2026-10-05 19:10:12' WHERE code = 10;
UPDATE topic SET display_name_en = 'Biomedical Convergence Engineering', display_name_ja = 'バイオメディカル融合学科', updated_at = '2026-10-05 19:10:12' WHERE code = 11;
UPDATE topic SET display_name_en = 'Precision Medicine & Medical Device', display_name_ja = '精密医療・医療機器学科', updated_at = '2026-10-05 19:10:12' WHERE code = 12;

-- 앱과 다르던 학과 일본어 표시명
UPDATE topic SET display_name_ja = 'データサイエンス専攻', updated_at = '2026-10-05 19:10:12' WHERE code = 701;
UPDATE topic SET display_name_ja = '人工知能専攻', updated_at = '2026-10-05 19:10:12' WHERE code = 702;
