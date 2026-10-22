package com.fx.api.adapter.`in`.web.template

import com.fx.api.adapter.`in`.web.template.dto.NotificationTemplateResponse
import com.fx.api.application.port.`in`.template.NotificationTemplateQueryUseCase
import com.fx.common.annotation.hexagonal.WebInputAdapter
import io.github.seob7.Api
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@WebInputAdapter
@RequestMapping("/api/v1/notification-templates")
class NotificationTemplateApiAdapter(
    private val notificationTemplateQueryUseCase: NotificationTemplateQueryUseCase,
) : NotificationTemplateApiSwagger {

    @GetMapping
    override fun getTemplates(): ResponseEntity<Api<List<NotificationTemplateResponse>>> =
        Api.OK(NotificationTemplateResponse.from(notificationTemplateQueryUseCase.getTemplates()), "알림 문구 조회 성공")

}
