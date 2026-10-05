package com.fx.common.adapter.out.persistence

import com.fx.common.adapter.out.persistence.repository.NotificationTemplateRepository
import com.fx.common.adapter.out.persistence.repository.TopicRepository
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

    @Test
    fun `토픽은 기존 code 대역을 유지해 유형별로 시드된다`() {
        val catalog = catalogPersistenceAdapter.loadTopicCatalog()

        assertThat(catalog.topics).hasSize(SEEDED_TOPICS)
        assertThat(catalog.topicsOf(TopicType.NOTICE).map {
            it.code
        }).containsExactly(1, 2, 3, 4, 5)
        assertThat(catalog.topicsOf(TopicType.MEAL).map {
            it.code
        }).containsExactly(900, 901)
        assertThat(catalog.topicsOf(TopicType.MAJOR)).hasSize(61)
            .allSatisfy {
                assertThat(it.code).satisfiesAnyOf({ code ->
                    assertThat(code).isBetween(10, 12)
                }, { code ->
                    assertThat(code).isBetween(100, 805)
                })
                assertThat(it.college).isNotNull()
            }
        assertThat(catalog.topics).allMatch {
            it.crawlEnabled && it.visible
        }
        assertThat(catalog.topicsOf(TopicType.NOTICE) + catalog.topicsOf(TopicType.MEAL)).allMatch {
            it.college == null
        }
    }

    @Test
    fun `토픽은 이름 · 코드 어느 쪽으로도 찾고 크롤링 URL 을 만든다`() {
        val catalog = catalogPersistenceAdapter.loadTopicCatalog()

        val generalNews = requireNotNull(catalog.findByName("GENERAL_NEWS"))
        assertThat(catalog.findByCode(1)).isEqualTo(generalNews)
        assertThat(generalNews.displayName.ko).isEqualTo("일반소식")
        assertThat(generalNews.noticeUrl()).isEqualTo("https://www.ut.ac.kr/cop/bbs/BBSMSTR_000000000059/selectBoardList.do")

        val computerEngineering = requireNotNull(catalog.findByCode(300))
        assertThat(computerEngineering.name).isEqualTo("COMPUTER_ENGINEERING")
        assertThat(computerEngineering.college?.collegeKey).isEqualTo("AI_CONVERGENCE")

        assertThat(catalog.findByName("STUDENT_CAFETERIA")?.noticeUrl())
            .isEqualTo("https://www.ut.ac.kr/prog/mealManage/MT01/kor/sub06_02_02_01/dayList.do")
    }

    @Test
    fun `단과대는 표시 순서대로 시드된다`() {
        val catalog = catalogPersistenceAdapter.loadTopicCatalog()

        assertThat(catalog.colleges.map {
            it.collegeKey
        }).containsExactly(
            "DEPRECATED", "ENGINEERING", "TRANSPORTATION_ENGINEERING", "AI_CONVERGENCE", "HUMANITIES",
            "SOCIAL_SCIENCES", "HEALTH_AND_LIFE_SCIENCE", "RAILROAD_SCIENCES", "FUTURE_CONVERGENCE",
        )
        assertThat(catalog.colleges.first {
            it.collegeKey == "ENGINEERING"
        }.displayName.resolve(Language.EN))
            .isEqualTo("College of Engineering")
    }

    @Test
    fun `표시명은 언어별로 해석되고 번역이 없으면 한국어를 쓴다`() {
        val catalog = catalogPersistenceAdapter.loadTopicCatalog()

        val translated = requireNotNull(catalog.findByName("MECHANICAL_ENGINEERING"))
        assertThat(translated.displayName.resolve(Language.EN)).isEqualTo("Mechanical Engineering")
        assertThat(requireNotNull(catalog.findByName("GENERAL_NEWS")).displayName.resolve(Language.JA)).isEqualTo("お知らせ")

        // 폐지 학과의 단과대는 레거시처럼 번역 없이 "DEPRECATED" 를 내보낸다
        val deprecated = requireNotNull(catalog.findByName("AI_ROBOTICS_ENGINEERING"))
        assertThat(deprecated.college?.displayName?.resolve(Language.EN)).isEqualTo("DEPRECATED")
    }

    @Test
    fun `모든 토픽과 알림 문구에 영어 · 일본어 번역이 있다`() {
        val untranslatedTopics = catalogPersistenceAdapter.loadTopicCatalog().topics.filter {
            it.displayName.en == null || it.displayName.ja == null
        }
        val untranslatedTemplates = notificationTemplateRepository.findAll().filter {
            it.text.en == null || it.text.ja == null
        }

        assertThat(untranslatedTopics.map {
            it.name
        }).isEmpty()
        assertThat(untranslatedTemplates.map {
            it.templateKey
        }).isEmpty()
    }

    @Test
    fun `토픽별 요약 여부는 모두 켜진 채로 시드되고 바꾸면 카탈로그에 반영된다`() {
        assertThat(catalogPersistenceAdapter.loadTopicCatalog().topics).allMatch {
            it.summaryEnabled
        }

        topicRepository.findAll().first {
            it.name == "GENERAL_NEWS"
        }.changeSummaryEnabled(false)
        entityManager.flush()

        assertThat(requireNotNull(catalogPersistenceAdapter.loadTopicCatalog().findByName("GENERAL_NEWS")).summaryEnabled).isFalse()
    }

    @Test
    fun `삭제된 토픽은 카탈로그에서 빠진다`() {
        val topic = topicRepository.findAll().first {
            it.name == "GENERAL_NEWS"
        }
        topic.delete(LocalDateTime.now())
        entityManager.flush()

        val catalog = catalogPersistenceAdapter.loadTopicCatalog()

        assertThat(catalog.findByName("GENERAL_NEWS")).isNull()
        assertThat(catalog.topics).hasSize(SEEDED_TOPICS - 1)
    }

    @Test
    fun `시드된 알림 문구는 키별 placeholder 규칙을 지킨다`() {
        val templates = notificationTemplateRepository.findAll()

        assertThat(templates.map {
            it.templateKey
        }).containsExactlyInAnyOrderElementsOf(NotificationTemplateKey.entries)
        templates.forEach { template ->
            assertThatCode {
                template.templateKey.validate(template.text)
            }.doesNotThrowAnyException()
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

    companion object {
        /** 공지 5 · 학과 61 (폐지 학과 3 포함) · 학식 2 */
        private const val SEEDED_TOPICS = 68
    }

}
