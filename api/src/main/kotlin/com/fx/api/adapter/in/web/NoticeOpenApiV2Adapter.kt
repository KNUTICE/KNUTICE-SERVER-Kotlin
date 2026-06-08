package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.notice.NoticeResponseV2
import com.fx.api.adapter.`in`.web.dto.notice.NoticeSearchParamV2
import com.fx.api.adapter.`in`.web.swagger.NoticeOpenApiV2Swagger
import com.fx.api.application.port.`in`.NoticeQueryUseCase
import com.fx.global.annotation.hexagonal.WebInputAdapter
import io.github.seob7.Api
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@WebInputAdapter
@RequestMapping("/open-api/v2/notices")
class NoticeOpenApiV2Adapter(
    private val noticeQueryUseCase: NoticeQueryUseCase
) : NoticeOpenApiV2Swagger {

    @GetMapping
    override fun getNotices(
        @ModelAttribute noticeSearchParamV2: NoticeSearchParamV2,
        @PageableDefault(sort = ["nttId"], direction = Sort.Direction.DESC, size = 20) pageable: Pageable
    ): ResponseEntity<Api<List<NoticeResponseV2>>> =
        Api.OK(NoticeResponseV2.from(
            noticeQueryUseCase.getNotices(noticeSearchParamV2.toCommand(pageable)))
        )

    @GetMapping("/{nttId}")
    override fun getNotice(@PathVariable nttId: Long): ResponseEntity<Api<NoticeResponseV2>> =
        Api.OK(NoticeResponseV2.from(noticeQueryUseCase.getNotice(nttId)))

}
