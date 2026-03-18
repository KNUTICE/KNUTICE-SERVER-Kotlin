package com.fx.api.application.port.`in`

import com.fx.api.application.port.`in`.dto.TipSaveCommand

interface TipCommandUseCase {

    suspend fun saveTip(tipSaveCommand: TipSaveCommand): Boolean
    suspend fun deleteTip(tipId: String): Boolean

}