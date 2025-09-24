package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.notice.NoticeResponse
import com.fx.api.adapter.`in`.web.dto.notice.NoticeSearchParam
import com.fx.api.adapter.`in`.web.dto.notice.NoticeSummaryResponse
import com.fx.api.adapter.`in`.web.swagger.NoticeOpenApiSwagger
import com.fx.api.application.port.`in`.NoticeQueryUseCase
import com.fx.global.annotation.hexagonal.WebInputAdapter
import com.fx.global.api.Api
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
) : NoticeOpenApiSwagger {

    @GetMapping
    override fun getNotices(
        @ModelAttribute noticeSearchParam: NoticeSearchParam,
        @PageableDefault(sort = ["nttId"], direction = Sort.Direction.DESC, size = 20) pageable: Pageable
    ): ResponseEntity<Api<List<NoticeResponse>>> =
        Api.OK(NoticeResponse.from(
            noticeQueryUseCase.getNotices(noticeSearchParam.toCommand(pageable)))
        )

    @GetMapping("/{nttId}")
    override fun getNotice(@PathVariable nttId: Long): ResponseEntity<Api<NoticeResponse>> =
        Api.OK(NoticeResponse.from(noticeQueryUseCase.getNotice(nttId)))

    @GetMapping("/summary/{nttId}")
    override fun getNoticeSummary(@PathVariable nttId: Long): ResponseEntity<Api<NoticeSummaryResponse>> =
        Api.OK(NoticeSummaryResponse.from(noticeQueryUseCase.getNoticeSummary(nttId)))

}