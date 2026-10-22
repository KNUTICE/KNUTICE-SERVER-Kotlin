package com.fx.api.adapter.`in`.web.college

import com.fx.api.adapter.`in`.web.college.dto.CollegeResponse
import io.github.seob7.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity

@Tag(name = "단과대 관리 API - ADMIN")
interface CollegeApiSwagger {

    @Operation(summary = "단과대 목록", description = "삭제되지 않은 단과대를 표시 순서대로 조회합니다.<br>" +
            "topicCount 는 삭제되지 않은 소속 학과 수입니다.")
    fun getColleges(): ResponseEntity<Api<List<CollegeResponse>>>

}
