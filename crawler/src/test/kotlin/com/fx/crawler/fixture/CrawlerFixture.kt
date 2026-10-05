package com.fx.crawler.fixture

import com.fx.common.domain.TopicType
import com.fx.common.domain.catalog.CollegeView
import com.fx.common.domain.catalog.NotificationTemplateCatalog
import com.fx.common.domain.catalog.TopicView
import com.fx.common.domain.i18n.LocalizedText
import com.fx.common.domain.notice.Notice
import com.fx.common.domain.notification.NotificationTemplateKey
import com.fx.persistence.withId
import java.time.LocalDate

object CrawlerFixture {

    val GENERAL_NEWS = topic(1, "GENERAL_NEWS", TopicType.NOTICE, LocalizedText("일반소식", "General News", "一般ニュース"))
    val COMPUTER_SOFTWARE = topic(
        300, "COMPUTER_SOFTWARE", TopicType.MAJOR, LocalizedText("컴퓨터소프트웨어학과"),
        college = CollegeView(10L, "ENGINEERING", LocalizedText("공과대학"), 1),
    )
    val STUDENT_CAFETERIA = topic(900, "STUDENT_CAFETERIA", TopicType.MEAL, LocalizedText("학생식당", "Student Cafeteria", null))

    /** ko 는 레거시 문구, en 은 번역이 있고 ja 는 없는(ko 로 대체되는) 문구 */
    val TEMPLATES = NotificationTemplateCatalog(
        mapOf(
            NotificationTemplateKey.NOTICE_BODY_MULTIPLE to LocalizedText("{title} 외 {count}개의 소식이 있습니다.", "{title} and {count} more", null),
            NotificationTemplateKey.MEAL_HEADER to LocalizedText("{date} {mealName} 메뉴", "{mealName} menu on {date}", null),
            NotificationTemplateKey.MEAL_SECTION_KOREAN to LocalizedText("[한식]", "[Korean]", null),
            NotificationTemplateKey.MEAL_SECTION_TOP to LocalizedText("[일품]", "[Special]", null),
            NotificationTemplateKey.MEAL_EMPTY to LocalizedText("등록된 식단 정보가 없습니다.", "No menu.", null),
            NotificationTemplateKey.SEAT_ALERT_TITLE to LocalizedText("빈자리 알림", "Seat available", "空席のお知らせ"),
            NotificationTemplateKey.SEAT_ALERT_BODY to LocalizedText("{roomName} {seatNumber}번 좌석이 비었습니다!", "Seat {seatNumber} in {roomName} is available!", null),
        )
    )

    fun notice(nttId: Long, title: String = "공지 $nttId", topic: TopicView = GENERAL_NEWS, imageUrl: String? = null): Notice =
        Notice.crawled(
            nttId = nttId,
            topic = topic,
            title = title,
            department = "학사팀",
            contentUrl = "https://www.ut.ac.kr/notice?nttId=$nttId",
            contentImageUrl = imageUrl,
            registrationDate = LocalDate.of(2026, 9, 30),
            isAttachment = false,
        ).withId(nttId * 10)

    private fun topic(code: Int, name: String, type: TopicType, displayName: LocalizedText, college: CollegeView? = null) =
        TopicView(
            id = code.toLong(),
            code = code,
            name = name,
            topicType = type,
            displayName = displayName,
            college = college,
            rootDomain = "https://www.ut.ac.kr",
            bbsPath = "/cop/bbs/$name/selectBoardList.do",
            crawlEnabled = true,
            visible = true,
        )

}
