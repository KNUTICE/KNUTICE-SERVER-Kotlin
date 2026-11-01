package com.fx.api.adapter.`in`.web.template

import com.fx.api.adapter.`in`.web.template.dto.NotificationTemplateResponse
import io.github.seob7.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity

@Tag(name = "알림 문구 관리 API - ADMIN")
interface NotificationTemplateApiSwagger {

    @Operation(summary = "알림 문구 목록", description = "FCM 알림 문구를 등록 순으로 조회합니다.<br>" +
            "placeholders 는 문구에 반드시 들어가야 하는 {이름} 목록입니다.")
    fun getTemplates(): ResponseEntity<Api<List<NotificationTemplateResponse>>>

}
