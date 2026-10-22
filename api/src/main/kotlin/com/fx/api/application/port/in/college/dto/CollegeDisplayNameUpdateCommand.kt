package com.fx.api.application.port.`in`.college.dto

import com.fx.common.domain.i18n.LocalizedText

data class CollegeDisplayNameUpdateCommand(
    val collegeKey: String,
    val displayName: LocalizedText,
)
