package com.fx.api.adapter.out.persistence

import com.fx.common.domain.notification.NotificationTemplateKey
import com.fx.persistence.MySqlContainerConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import

/** 실제 MySQL 에서 Flyway 로 시드된 알림 문구를 조회한다. */
@DataJpaTest(properties = ["MYSQL_URL=unused", "MYSQL_DATABASE=unused", "MYSQL_USERNAME=unused", "MYSQL_PASSWORD=unused"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MySqlContainerConfig::class, NotificationTemplatePersistenceAdapter::class)
class NotificationTemplatePersistenceAdapterTest @Autowired constructor(
    private val notificationTemplatePersistenceAdapter: NotificationTemplatePersistenceAdapter,
) {

    @Test
    fun `알림 문구를 등록 순으로 조회한다`() {
        val templates = notificationTemplatePersistenceAdapter.findAll()

        assertThat(templates.map { it.templateKey }).containsExactly(
            NotificationTemplateKey.NOTICE_BODY_MULTIPLE,
            NotificationTemplateKey.MEAL_HEADER,
            NotificationTemplateKey.MEAL_SECTION_KOREAN,
            NotificationTemplateKey.MEAL_SECTION_TOP,
            NotificationTemplateKey.MEAL_EMPTY,
            NotificationTemplateKey.SEAT_ALERT_TITLE,
            NotificationTemplateKey.SEAT_ALERT_BODY,
        )
        assertThat(templates.first().text.ko).isEqualTo("{title} 외 {count}개의 소식이 있습니다.")
    }

}
