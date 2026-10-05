package com.fx.api.application.port.`in`.notice.dto

import com.fx.common.domain.TopicType
import java.time.LocalDate

/** 관리자 공지 등록 · 수정. [topicName] 은 [topicType] 에 속한 토픽이어야 한다. */
data class NoticeCommand(
    val nttId: Long,
    val title: String,
    val contentUrl: String,
    val contentSummary: String? = null,
    val contentImageUrl: String? = null,
    val department: String,
    val registrationDate: LocalDate,
    val isAttachment: Boolean,
    val topicName: String,
    val topicType: TopicType,
)
