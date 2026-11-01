package com.fx.api.application.port.`in`.college

import com.fx.api.domain.CollegeDetail

interface CollegeQueryUseCase {

    fun getColleges(): List<CollegeDetail>

}
