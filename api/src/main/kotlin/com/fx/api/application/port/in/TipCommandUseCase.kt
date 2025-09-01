package com.fx.api.application.port.`in`

import com.fx.api.application.port.`in`.dto.TipCommand

interface TipCommandUseCase {

    fun saveTip(tipCommand: TipCommand): Boolean
    fun deleteTip(tipId: String): Boolean

}