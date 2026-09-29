package com.fx.common.domain.notification

import com.fx.common.domain.i18n.LocalizedText
import com.fx.persistence.BaseEntity
import jakarta.persistence.AttributeOverride
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

/** 알림 문구 최대 길이 — `text_*` 컬럼 길이와 같아야 한다. */
const val NOTIFICATION_TEXT_MAX_LENGTH = 500

/** 알림 문구 설명 최대 길이 — `description` 컬럼 길이와 같아야 한다. */
const val NOTIFICATION_DESCRIPTION_MAX_LENGTH = 200

/**
 * FCM 알림 문구. 관리자는 문구만 바꿀 수 있고 키는 코드([NotificationTemplateKey])가 정한다.
 * 문구를 바꿀 때마다 키의 placeholder 규칙을 검증한다.
 */
@Entity
@Table(
    name = "notification_template",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_notification_template_template_key", columnNames = ["template_key"]),
    ],
)
class NotificationTemplate(
    templateKey: NotificationTemplateKey,
    text: LocalizedText,
    description: String,
) : BaseEntity() {

    @Enumerated(EnumType.STRING)
    @Column(name = "template_key", nullable = false, updatable = false, length = 50, comment = "템플릿 키 (코드의 NotificationTemplateKey)")
    val templateKey: NotificationTemplateKey = templateKey

    @Embedded
    @AttributeOverride(name = "ko", column = Column(name = "text_ko", nullable = false, length = NOTIFICATION_TEXT_MAX_LENGTH, comment = "문구 (한국어)"))
    @AttributeOverride(name = "en", column = Column(name = "text_en", nullable = true, length = NOTIFICATION_TEXT_MAX_LENGTH, comment = "문구 (영어)"))
    @AttributeOverride(name = "ja", column = Column(name = "text_ja", nullable = true, length = NOTIFICATION_TEXT_MAX_LENGTH, comment = "문구 (일본어)"))
    var text: LocalizedText = text
        protected set

    @Column(name = "description", nullable = false, length = NOTIFICATION_DESCRIPTION_MAX_LENGTH, comment = "관리자 화면용 설명")
    var description: String = description
        protected set

    init {
        templateKey.validate(text)
    }

    fun changeText(text: LocalizedText) {
        templateKey.validate(text)
        this.text = text
    }

}
