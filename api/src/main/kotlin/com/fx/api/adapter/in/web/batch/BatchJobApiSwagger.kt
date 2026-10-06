package com.fx.api.adapter.`in`.web.batch

import com.fx.api.adapter.`in`.web.batch.dto.BatchJobResponse
import io.github.seob7.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity

@Tag(name = "배치 Job API - ADMIN")
interface BatchJobApiSwagger {

    @Operation(summary = "Job 목록", description = "스케줄 추가 · 수동 실행에 쓸 수 있는 Job 과 Job 별 파라미터를 조회합니다.<br>" +
            "parameters 는 모두 필수입니다. allowedValues 가 있으면 그중 하나를 고르고, null 이면 직접 입력합니다.")
    fun getJobs(): ResponseEntity<Api<List<BatchJobResponse>>>

}
