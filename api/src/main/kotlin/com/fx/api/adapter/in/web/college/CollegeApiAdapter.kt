package com.fx.api.adapter.`in`.web.college

import com.fx.api.adapter.`in`.web.college.dto.CollegeDisplayNameUpdateRequest
import com.fx.api.adapter.`in`.web.college.dto.CollegeResponse
import com.fx.api.application.port.`in`.college.CollegeCommandUseCase
import com.fx.api.application.port.`in`.college.CollegeQueryUseCase
import com.fx.common.annotation.hexagonal.WebInputAdapter
import io.github.seob7.Api
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@WebInputAdapter
@RequestMapping("/api/v1/colleges")
class CollegeApiAdapter(
    private val collegeQueryUseCase: CollegeQueryUseCase,
    private val collegeCommandUseCase: CollegeCommandUseCase,
) : CollegeApiSwagger {

    @GetMapping
    override fun getColleges(): ResponseEntity<Api<List<CollegeResponse>>> =
        Api.OK(CollegeResponse.from(collegeQueryUseCase.getColleges()), "단과대 조회 성공")

    @PatchMapping("/{collegeKey}")
    override fun updateDisplayName(
        @PathVariable collegeKey: String,
        @RequestBody @Valid request: CollegeDisplayNameUpdateRequest
    ): ResponseEntity<Api<CollegeResponse>> =
        Api.OK(
            CollegeResponse.from(collegeCommandUseCase.updateDisplayName(request.toCommand(collegeKey))),
            "단과대 표시명을 수정했습니다."
        )

}
