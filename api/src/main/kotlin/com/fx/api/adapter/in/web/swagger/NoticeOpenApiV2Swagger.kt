package com.fx.api.adapter.`in`.web.swagger

import com.fx.api.adapter.`in`.web.dto.notice.NoticeResponseV2
import com.fx.api.adapter.`in`.web.dto.notice.NoticeSearchParamV2
import com.fx.global.exception.errorcode.NoticeErrorCode
import com.fx.global.annotation.ApiExceptionExplanation
import com.fx.global.annotation.ApiResponseExplanations
import io.github.seob7.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable

@Tag(name = "공지 API")
interface NoticeOpenApiV2Swagger {

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
    @Operation(
        summary = "공지 목록을 조회/검색합니다. (정수형 topic)",
        description = "ex) /open-api/v2/notices?topicId=1&size=5&nttId=1081908&keyword=파이썬&sort=nttId,DESC <br>" +
                "[DEFAULT : sort=nttId,DESC&size=20] <br><br>" +
                "NoticeType: GENERAL_NEWS=1, SCHOLARSHIP_NEWS=2, EVENT_NEWS=3, ACADEMIC_NEWS=4, EMPLOYMENT_NEWS=5 <br>" +
                "MajorType: 공과대학 100~113, 교통공과대학 200~202, AI융합대학 300~303, 인문대학 400~405, " +
                "사회과학대학 500~509, 보건생명대학 600~607, 철도대학 700~706, 미래융합대학 800~805 <br><br>"
    )
    fun getNotices(
        @ModelAttribute noticeSearchParamV2: NoticeSearchParamV2,
        @PageableDefault(sort = ["nttId"], direction = Sort.Direction.DESC, size = 20) pageable: Pageable
    ): ResponseEntity<Api<List<NoticeResponseV2>>>

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
    @Operation(summary = "단일 공지 조회 (정수형 topic)", description = "nttId 로 공지를 조회합니다.")
    fun getNotice(@PathVariable nttId: Long): ResponseEntity<Api<NoticeResponseV2>>

}
