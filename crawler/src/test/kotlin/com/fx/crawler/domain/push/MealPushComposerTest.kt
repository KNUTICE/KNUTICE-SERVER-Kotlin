package com.fx.crawler.domain.push

import com.fx.common.domain.i18n.Language
import com.fx.crawler.domain.meal.Meal
import com.fx.crawler.fixture.CrawlerFixture.STUDENT_CAFETERIA
import com.fx.crawler.fixture.CrawlerFixture.TEMPLATES
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class MealPushComposerTest {

    private val date = LocalDate.of(2026, 9, 30)

    @Test
    fun `레거시와 같은 형식으로 한식 · 일품 메뉴를 쓴다`() {
        val meal = Meal(900, date, koreaMenus = listOf("김치찌개", "계란말이"), topMenus = listOf("돈까스"))

        val message = MealPushComposer.compose(STUDENT_CAFETERIA, meal, TEMPLATES).of(Language.KO).single()

        assertThat(message.title).isEqualTo("학생식당")
        assertThat(message.body).isEqualTo("2026-09-30 학생식당 메뉴\n[한식]\n김치찌개\n계란말이\n\n[일품]\n돈까스")
        assertThat(message.data).containsEntry("deeplink", "knutice://meal?topic=STUDENT_CAFETERIA").containsEntry("topic", "STUDENT_CAFETERIA")
    }

    @Test
    fun `일품만 있으면 빈 줄 없이 일품부터 쓴다`() {
        val meal = Meal(900, date, koreaMenus = emptyList(), topMenus = listOf("돈까스"))

        assertThat(MealPushComposer.compose(STUDENT_CAFETERIA, meal, TEMPLATES).of(Language.KO).single().body)
            .isEqualTo("2026-09-30 학생식당 메뉴\n[일품]\n돈까스")
    }

    @Test
    fun `머리말 · 구분 제목은 번역하고 메뉴는 원문 그대로 쓴다`() {
        val meal = Meal(900, date, koreaMenus = listOf("김치찌개"), topMenus = emptyList())

        val message = MealPushComposer.compose(STUDENT_CAFETERIA, meal, TEMPLATES).of(Language.EN).single()

        assertThat(message.title).isEqualTo("Student Cafeteria")
        assertThat(message.body).isEqualTo("Student Cafeteria menu on 2026-09-30\n[Korean]\n김치찌개")
    }

    @Test
    fun `메뉴가 모두 비어 있으면 식단 없음 문구를 쓴다`() {
        val meal = Meal(900, date, koreaMenus = emptyList(), topMenus = emptyList())

        assertThat(MealPushComposer.compose(STUDENT_CAFETERIA, meal, TEMPLATES).of(Language.KO).single().body)
            .isEqualTo("2026-09-30 학생식당 메뉴\n등록된 식단 정보가 없습니다.")
    }

}
