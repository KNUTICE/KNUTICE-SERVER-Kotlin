package com.fx.api.adapter.`in`.web.batch

import com.fx.api.adapter.`in`.web.batch.dto.BatchJobResponse
import com.fx.api.application.port.`in`.batch.BatchJobQueryUseCase
import com.fx.common.annotation.hexagonal.WebInputAdapter
import io.github.seob7.Api
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@WebInputAdapter
@RequestMapping("/api/v1/batch/jobs")
class BatchJobApiAdapter(
    private val batchJobQueryUseCase: BatchJobQueryUseCase,
) : BatchJobApiSwagger {

    @GetMapping
    override fun getJobs(): ResponseEntity<Api<List<BatchJobResponse>>> =
        Api.OK(BatchJobResponse.from(batchJobQueryUseCase.getJobs()), "Job 목록 조회 성공")

}
