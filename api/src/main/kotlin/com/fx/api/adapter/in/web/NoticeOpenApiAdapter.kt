package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.NoticeResponse
import com.fx.api.adapter.`in`.web.dto.NoticeSearchParam
import com.fx.api.application.port.`in`.NoticeQueryUseCase
import com.fx.global.annotation.hexagonal.WebInputAdapter
import com.fx.global.api.Api
import io.swagger.v3.oas.annotations.Operation
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@WebInputAdapter
@RequestMapping("/open-api/v1/notices")
class NoticeOpenApiAdapter(
    private val noticeQueryUseCase: NoticeQueryUseCase
) {

    @Operation(summary = "공지 목록을 조회/검색합니다.",
        description = "/open-api/v1/notices?type=GENERAL_NEWS&size=5&nttId=1081908&keyword=파이썬")
    @GetMapping
    fun getNotices(
        @ModelAttribute noticeSearchParam: NoticeSearchParam,
        @PageableDefault(sort = ["nttId"], direction = Sort.Direction.DESC, size = 20) pageable: Pageable
    ): ResponseEntity<Api<List<NoticeResponse>>> =
        Api.OK(NoticeResponse.from(
            noticeQueryUseCase.getNotices(noticeSearchParam.toCommand(pageable)))
        )

    @Operation(summary = "단일 공지 조회", description = "nttId 로 공지를 조회합니다.")
    @GetMapping("/{nttId}")
    fun getNotice(@PathVariable nttId: Long): ResponseEntity<Api<NoticeResponse>> =
        Api.OK(NoticeResponse.from(noticeQueryUseCase.getNotice(nttId)))

}