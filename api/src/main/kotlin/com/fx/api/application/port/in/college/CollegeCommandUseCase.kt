package com.fx.api.application.port.`in`.college

import com.fx.api.application.port.`in`.college.dto.CollegeDisplayNameUpdateCommand
import com.fx.api.domain.CollegeDetail

interface CollegeCommandUseCase {

    /** @throws com.fx.api.exception.CollegeException 없거나 삭제된 단과대 (COLLEGE_NOT_FOUND) */
    fun updateDisplayName(command: CollegeDisplayNameUpdateCommand): CollegeDetail

}
