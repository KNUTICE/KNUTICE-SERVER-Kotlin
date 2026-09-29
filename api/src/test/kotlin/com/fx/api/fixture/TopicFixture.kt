package com.fx.api.fixture

import com.fx.common.domain.TopicType
import com.fx.common.domain.catalog.CollegeView
import com.fx.common.domain.catalog.TopicCatalog
import com.fx.common.domain.catalog.TopicView
import com.fx.common.domain.i18n.LocalizedText

object TopicFixture {

    val ENGINEERING = CollegeView(
        id = 10L,
        collegeKey = "ENGINEERING",
        displayName = LocalizedText("공과대학", "College of Engineering", "工学部"),
        displayOrder = 2,
    )

    val GENERAL_NEWS = topic(id = 1L, code = 1, name = "GENERAL_NEWS", type = TopicType.NOTICE, displayName = LocalizedText("일반소식"))
    val SCHOLARSHIP_NEWS = topic(id = 2L, code = 2, name = "SCHOLARSHIP_NEWS", type = TopicType.NOTICE, displayName = LocalizedText("장학안내"))
    val HIDDEN_NEWS = topic(id = 5L, code = 5, name = "HIDDEN_NEWS", type = TopicType.NOTICE, displayName = LocalizedText("숨김"), visible = false)
    val COMPUTER_SOFTWARE = topic(
        id = 3L, code = 300, name = "COMPUTER_SOFTWARE", type = TopicType.MAJOR,
        displayName = LocalizedText("컴퓨터소프트웨어학과", "Computer Software"), college = ENGINEERING,
    )
    val STUDENT_CAFETERIA = topic(id = 4L, code = 900, name = "STUDENT_CAFETERIA", type = TopicType.MEAL, displayName = LocalizedText("학생식당"))

    val CATALOG = TopicCatalog(
        topics = listOf(STUDENT_CAFETERIA, COMPUTER_SOFTWARE, HIDDEN_NEWS, SCHOLARSHIP_NEWS, GENERAL_NEWS),
        colleges = listOf(ENGINEERING),
    )

    private fun topic(
        id: Long,
        code: Int,
        name: String,
        type: TopicType,
        displayName: LocalizedText,
        college: CollegeView? = null,
        visible: Boolean = true,
    ) = TopicView(
        id = id,
        code = code,
        name = name,
        topicType = type,
        displayName = displayName,
        college = college,
        rootDomain = "https://www.ut.ac.kr",
        bbsPath = "/cop/bbs/$name/selectBoardList.do",
        crawlEnabled = true,
        visible = visible,
    )

}
