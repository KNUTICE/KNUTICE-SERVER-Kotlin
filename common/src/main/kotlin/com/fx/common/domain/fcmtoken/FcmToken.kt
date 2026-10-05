package com.fx.common.domain.fcmtoken

import com.fx.common.domain.DeviceType
import com.fx.common.domain.i18n.Language
import com.fx.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

/** FCM 토큰 최대 길이 — `token` 컬럼 길이와 같아야 한다. */
const val FCM_TOKEN_MAX_LENGTH = 512

/** 언어 값 최대 길이 — `language` 컬럼 길이와 같아야 한다. */
const val FCM_TOKEN_LANGUAGE_MAX_LENGTH = 10

/**
 * 앱 설치 단위의 FCM 등록 토큰. 구독 토픽은 [FcmTokenSubscription] 에 따로 둔다.
 *
 * - 토큰 값이 바뀌면 행을 새로 만들지 않고 [changeToken] 으로 바꿔 id · 구독을 유지한다.
 * - [language] 는 enum 이 아닌 문자열이다. 어떤 값이 들어 있어도 발송 시 [resolveLanguage] 로 해석한다.
 */
@Entity
@Table(
    name = "fcm_token",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_fcm_token_token", columnNames = ["token"]),
    ],
)
class FcmToken(
    token: String,
    deviceType: DeviceType,
    language: String = Language.DEFAULT.code,
) : BaseEntity() {

    @Column(name = "token", nullable = false, length = FCM_TOKEN_MAX_LENGTH, comment = "FCM 등록 토큰 (대소문자 구분)")
    var token: String = token
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 20, comment = "iOS / AOS / UNKNOWN")
    var deviceType: DeviceType = deviceType
        protected set

    @Column(name = "is_active", nullable = false, comment = "발송 대상 여부")
    var isActive: Boolean = true
        protected set

    @Column(name = "language", nullable = false, length = FCM_TOKEN_LANGUAGE_MAX_LENGTH, comment = "알림 언어 (발송 시 해석)")
    var language: String = language
        protected set

    init {
        require(token.isNotBlank()) {
            "FCM 토큰은 비어 있을 수 없습니다."
        }
    }

    fun activate() {
        isActive = true
    }

    fun deactivate() {
        isActive = false
    }

    /** 앱이 새 토큰을 받았을 때. 같은 행을 유지해 구독과 연결된 데이터가 그대로 남는다. */
    fun changeToken(newToken: String) {
        require(newToken.isNotBlank()) {
            "FCM 토큰은 비어 있을 수 없습니다."
        }
        token = newToken
        isActive = true
    }

    /** 앱에서 알림 언어를 바꿨을 때. 지원 언어의 코드(`ko` · `en` · `ja`)로 저장한다. */
    fun changeLanguage(language: Language) {
        this.language = language.code
    }

    fun resolveLanguage(): Language =
        Language.from(language)

}
