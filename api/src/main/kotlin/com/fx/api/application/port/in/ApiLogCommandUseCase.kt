package com.fx.api.application.port.`in`

import com.fx.api.application.port.`in`.dto.ApiLogSaveCommand

interface ApiLogCommandUseCase {

    fun recordApiLog(apiLogSaveCommand: ApiLogSaveCommand)

}