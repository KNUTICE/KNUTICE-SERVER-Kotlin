package com.fx.api.adapter.`in`.web.college

import com.fx.api.adapter.`in`.web.college.dto.CollegeResponse
import com.fx.api.application.port.`in`.college.CollegeQueryUseCase
import com.fx.common.annotation.hexagonal.WebInputAdapter
import io.github.seob7.Api
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@WebInputAdapter
@RequestMapping("/api/v1/colleges")
class CollegeApiAdapter(
    private val collegeQueryUseCase: CollegeQueryUseCase,
) : CollegeApiSwagger {

    @GetMapping
    override fun getColleges(): ResponseEntity<Api<List<CollegeResponse>>> =
        Api.OK(CollegeResponse.from(collegeQueryUseCase.getColleges()), "단과대 조회 성공")

}
