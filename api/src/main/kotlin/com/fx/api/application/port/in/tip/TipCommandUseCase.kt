package com.fx.api.application.port.`in`.tip

import com.fx.api.application.port.`in`.tip.dto.TipSaveCommand

interface TipCommandUseCase {

    fun saveTip(tipSaveCommand: TipSaveCommand): Boolean
    fun deleteTip(tipId: String): Boolean

}