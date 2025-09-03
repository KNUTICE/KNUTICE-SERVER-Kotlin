package com.fx.api.application.port.`in`

import com.fx.api.application.port.`in`.dto.TipSaveCommand

interface TipCommandUseCase {

    fun saveTip(tipSaveCommand: TipSaveCommand): Boolean
    fun deleteTip(tipId: String): Boolean

}