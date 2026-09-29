package com.fx.api.domain

import com.fx.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

/** 문의 내용 최대 길이 — `content` 컬럼 길이 · 요청 검증과 같아야 한다. */
const val REPORT_CONTENT_MAX_LENGTH = 500

/** 앱에서 보낸 문의. 등록되면 Slack 으로도 알린다. */
@Entity
@Table(name = "report")
class Report(
    fcmTokenId: Long,
    content: String,
    deviceName: String,
    version: String,
) : BaseEntity() {

    @Column(name = "fcm_token_id", nullable = false, updatable = false, comment = "문의한 FCM 토큰 ID")
    val fcmTokenId: Long = fcmTokenId

    @Column(name = "content", nullable = false, length = REPORT_CONTENT_MAX_LENGTH, comment = "문의 내용")
    val content: String = content

    @Column(name = "device_name", nullable = false, length = 100, comment = "기기명")
    val deviceName: String = deviceName

    @Column(name = "version", nullable = false, length = 50, comment = "앱 버전")
    val version: String = version

}
