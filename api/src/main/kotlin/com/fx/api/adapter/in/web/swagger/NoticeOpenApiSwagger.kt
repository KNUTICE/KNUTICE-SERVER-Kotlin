package com.fx.api.adapter.`in`.web.swagger

import com.fx.api.adapter.`in`.web.dto.notice.NoticeResponse
import com.fx.api.adapter.`in`.web.dto.notice.NoticeSearchParam
import com.fx.api.adapter.`in`.web.dto.notice.NoticeSummaryResponse
import com.fx.api.exception.errorcode.NoticeErrorCode
import com.fx.global.annotation.ApiExceptionExplanation
import com.fx.global.annotation.ApiResponseExplanations
import com.fx.global.api.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable

@Tag(name = "공지 API")
interface NoticeOpenApiSwagger {

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "공지 목록 조회 실패",
                description = "서버에 다음 공지가 존재하지 않는 경우",
                value = NoticeErrorCode::class,
                constant = "NOTICE_NOT_FOUND"
            ),
        ]
    )
    @Operation(summary = "공지 목록을 조회/검색합니다.",
        description = "ex) /open-api/v1/notices?topic=GENERAL_NEWS&size=5&nttId=1081908&keyword=파이썬&sort=nttId,DESC <br>" +
                "[DEFAULT : sort=nttId,DESC&size=20]")
    fun getNotices(
        @ModelAttribute noticeSearchParam: NoticeSearchParam,
        @PageableDefault(sort = ["nttId"], direction = Sort.Direction.DESC, size = 20) pageable: Pageable
    ): ResponseEntity<Api<List<NoticeResponse>>>



    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "단일 공지 조회 실패",
                description = "서버에 해당하는 공지가 존재하지 않는 경우",
                value = NoticeErrorCode::class,
                constant = "NOTICE_NOT_FOUND"
            ),
        ]
    )
    @Operation(summary = "단일 공지 조회", description = "nttId 로 공지를 조회합니다.")
    fun getNotice(@PathVariable nttId: Long): ResponseEntity<Api<NoticeResponse>>

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "공지 요약 조회 실패",
                description = "서버에 해당하는 공지가 존재하지 않는 경우",
                value = NoticeErrorCode::class,
                constant = "NOTICE_NOT_FOUND"
            ),
            ApiExceptionExplanation(
                name = "공지 요약 조회 실패",
                description = "서버에 공지는 존재하지만 요약 내용이 없는 경우",
                value = NoticeErrorCode::class,
                constant = "SUMMARY_CONTENT_NOT_FOUND"
            )
        ]
    )
    @Operation(summary = "공지 요약 조회", description = "nttId 로 공지 요약을 조회합니다. <br>" +
            "`contentSummary` 존재하지 않는 경우 예외가 발생됩니다.")
    fun getNoticeSummary(@PathVariable nttId: Long): ResponseEntity<Api<NoticeSummaryResponse>>

}