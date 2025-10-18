package com.fx.api.adapter.`in`.web.swagger

import com.fx.api.adapter.`in`.web.dto.notice.NoticeRequest
import com.fx.api.exception.errorcode.NoticeErrorCode
import com.fx.global.annotation.ApiExceptionExplanation
import com.fx.global.annotation.ApiResponseExplanations
import com.fx.global.domain.TopicType
import io.github.seob7.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "공지 관리 API")
interface NoticeApiSwagger {

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "수정 실패",
                description = "수정하려는 공지가 존재하지 않는 경우",
                value = NoticeErrorCode::class,
                constant = "NOTICE_NOT_FOUND"
            ),
        ]
    )
    @Operation(summary = "공지 수정", description = "Request Body 에 값이 없는 경우 null 처리됩니다.")
    fun updateNotice(
        @RequestBody @Valid noticeRequest: NoticeRequest,
        @RequestParam type: TopicType,
    ): ResponseEntity<Api<Boolean>>

}