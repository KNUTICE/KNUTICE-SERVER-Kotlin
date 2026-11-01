package com.fx.api.adapter.`in`.web.college

import com.fx.api.adapter.`in`.web.college.dto.CollegeDisplayNameUpdateRequest
import com.fx.api.adapter.`in`.web.college.dto.CollegeResponse
import com.fx.api.exception.errorcode.CollegeErrorCode
import com.fx.common.annotation.ApiExceptionExplanation
import com.fx.common.annotation.ApiResponseExplanations
import io.github.seob7.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "단과대 관리 API - ADMIN")
interface CollegeApiSwagger {

    @Operation(summary = "단과대 목록", description = "삭제되지 않은 단과대를 표시 순서대로 조회합니다.<br>" +
            "topicCount 는 삭제되지 않은 소속 학과 수입니다.")
    fun getColleges(): ResponseEntity<Api<List<CollegeResponse>>>

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "수정 실패 - 단과대",
                description = "없거나 삭제된 collegeKey 인 경우",
                value = CollegeErrorCode::class,
                constant = "COLLEGE_NOT_FOUND"
            ),
        ]
    )
    @Operation(summary = "단과대 표시명 수정", description = "표시명을 언어별로 통째로 바꿉니다. ko 는 필수이고, en · ja 는 null 이나 빈 문자열이면 비웁니다. 모두 최대 100자입니다.<br>" +
            "앱 응답에는 바로 반영되고, crawler 가 보내는 알림에는 5분 안에 반영됩니다.")
    fun updateDisplayName(
        @PathVariable collegeKey: String,
        @RequestBody request: CollegeDisplayNameUpdateRequest
    ): ResponseEntity<Api<CollegeResponse>>

}
