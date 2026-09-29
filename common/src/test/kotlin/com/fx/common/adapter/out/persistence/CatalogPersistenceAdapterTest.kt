package com.fx.common.adapter.out.persistence

import com.fx.common.adapter.out.persistence.repository.NotificationTemplateRepository
import com.fx.common.adapter.out.persistence.repository.TopicRepository
import com.fx.common.domain.CrawlableType
import com.fx.common.domain.MajorType
import com.fx.common.domain.MealType
import com.fx.common.domain.NoticeType
import com.fx.common.domain.TopicType
import com.fx.common.domain.i18n.Language
import com.fx.common.domain.notification.NotificationTemplateKey
import com.fx.persistence.MySqlContainerConfig
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import java.time.LocalDateTime

/**
 * Flyway 가 만든 스키마 · 시드를 실제 MySQL 에서 검증한다.
 * `ddl-auto=validate` 이므로 엔티티 매핑과 스키마가 어긋나면 컨텍스트가 뜨지 않는다.
 */
@DataJpaTest(properties = ["spring.jpa.hibernate.ddl-auto=validate"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MySqlContainerConfig::class, CatalogPersistenceAdapter::class)
class CatalogPersistenceAdapterTest @Autowired constructor(
    private val catalogPersistenceAdapter: CatalogPersistenceAdapter,
    private val topicRepository: TopicRepository,
    private val notificationTemplateRepository: NotificationTemplateRepository,
    private val entityManager: EntityManager,
) {

    private val legacyTypes: List<CrawlableType> = NoticeType.entries + MajorType.entries + MealType.entries

    @Test
    fun `시드된 토픽은 레거시 enum 과 같다`() {
        val catalog = catalogPersistenceAdapter.loadTopicCatalog()

        assertThat(catalog.topics).hasSize(legacyTypes.size)
        legacyTypes.forEach { type ->
            val topic = requireNotNull(catalog.findByCode(type.code)) { "시드에 없는 토픽: ${type.topicName}" }

            assertThat(topic.name).isEqualTo(type.topicName)
            assertThat(topic.topicType).isEqualTo(type.toTopicType())
            assertThat(topic.noticeUrl()).isEqualTo(type.getNoticeUrl())
            assertThat(topic.displayName.ko).isEqualTo(type.category)
            assertThat(topic.crawlEnabled).isTrue()
            assertThat(topic.visible).isTrue()
            assertThat(catalog.findByName(type.topicName)).isEqualTo(topic)

            if (type is MajorType) {
                assertThat(topic.college?.collegeKey).isEqualTo(type.college)
            } else {
                assertThat(topic.college).isNull()
            }
        }
    }

    @Test
    fun `토픽 순서는 레거시 enum 선언 순서와 같다`() {
        val catalog = catalogPersistenceAdapter.loadTopicCatalog()

        assertThat(catalog.topicsOf(TopicType.NOTICE).map { it.code }).containsExactlyElementsOf(NoticeType.entries.map { it.code })
        assertThat(catalog.topicsOf(TopicType.MAJOR).map { it.code }).containsExactlyElementsOf(MajorType.entries.map { it.code })
        assertThat(catalog.topicsOf(TopicType.MEAL).map { it.code }).containsExactlyElementsOf(MealType.entries.map { it.code })
    }

    @Test
    fun `단과대는 학과 enum 에 처음 나온 순서로 시드된다`() {
        val catalog = catalogPersistenceAdapter.loadTopicCatalog()

        assertThat(catalog.colleges.map { it.collegeKey })
            .containsExactlyElementsOf(MajorType.entries.map { it.college }.distinct())
        assertThat(catalog.colleges.first { it.collegeKey == "ENGINEERING" }.displayName.resolve(Language.EN))
            .isEqualTo("College of Engineering")
    }

    @Test
    fun `학과 표시명은 언어별로 해석되고 번역이 없으면 한국어를 쓴다`() {
        val catalog = catalogPersistenceAdapter.loadTopicCatalog()

        val translated = requireNotNull(catalog.findByName("MECHANICAL_ENGINEERING"))
        assertThat(translated.displayName.resolve(Language.EN)).isEqualTo("Mechanical Engineering")

        val deprecated = requireNotNull(catalog.findByName("AI_ROBOTICS_ENGINEERING"))
        assertThat(deprecated.displayName.resolve(Language.EN)).isEqualTo(deprecated.displayName.ko)
        assertThat(deprecated.college?.displayName?.resolve(Language.KO)).isEqualTo("DEPRECATED")
    }

    @Test
    fun `삭제된 토픽은 카탈로그에서 빠진다`() {
        val topic = topicRepository.findAll().first { it.name == "GENERAL_NEWS" }
        topic.delete(LocalDateTime.now())
        entityManager.flush()

        val catalog = catalogPersistenceAdapter.loadTopicCatalog()

        assertThat(catalog.findByName("GENERAL_NEWS")).isNull()
        assertThat(catalog.topics).hasSize(legacyTypes.size - 1)
    }

    @Test
    fun `시드된 알림 문구는 키별 placeholder 규칙을 지킨다`() {
        val templates = notificationTemplateRepository.findAll()

        assertThat(templates.map { it.templateKey }).containsExactlyInAnyOrderElementsOf(NotificationTemplateKey.entries)
        templates.forEach { template ->
            assertThatCode { template.templateKey.validate(template.text) }.doesNotThrowAnyException()
        }
    }

    @Test
    fun `시드된 알림 문구는 레거시 하드코딩 문구와 같은 결과를 만든다`() {
        val catalog = catalogPersistenceAdapter.loadNotificationTemplateCatalog()
        val ko = Language.KO

        assertThat(catalog.render(NotificationTemplateKey.NOTICE_BODY_MULTIPLE, ko, mapOf("title" to "장학 공지", "count" to 2)))
            .isEqualTo("장학 공지 외 2개의 소식이 있습니다.")
        assertThat(catalog.render(NotificationTemplateKey.MEAL_HEADER, ko, mapOf("date" to "2026-09-29", "mealName" to "학생식당")))
            .isEqualTo("2026-09-29 학생식당 메뉴")
        assertThat(catalog.render(NotificationTemplateKey.MEAL_SECTION_KOREAN, ko)).isEqualTo("[한식]")
        assertThat(catalog.render(NotificationTemplateKey.MEAL_SECTION_TOP, ko)).isEqualTo("[일품]")
        assertThat(catalog.render(NotificationTemplateKey.MEAL_EMPTY, ko)).isEqualTo("등록된 식단 정보가 없습니다.")
        assertThat(catalog.render(NotificationTemplateKey.SEAT_ALERT_TITLE, ko)).isEqualTo("빈자리 알림")
        assertThat(catalog.render(NotificationTemplateKey.SEAT_ALERT_BODY, ko, mapOf("roomName" to "제1열람실", "seatNumber" to 12)))
            .isEqualTo("제1열람실 12번 좌석이 비었습니다!")
    }

    private fun CrawlableType.toTopicType(): TopicType =
        when (this) {
            is NoticeType -> TopicType.NOTICE
            is MajorType -> TopicType.MAJOR
            is MealType -> TopicType.MEAL
            else -> error("알 수 없는 토픽 유형: $this")
        }

}
