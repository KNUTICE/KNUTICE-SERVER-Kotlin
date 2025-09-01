package com.fx.api.application.service

import com.fx.api.application.port.`in`.TipQueryUseCase
import com.fx.api.application.port.`in`.TipCommandUseCase
import com.fx.api.application.port.`in`.dto.TipCommand
import com.fx.api.application.port.out.TipPersistencePort
import com.fx.api.domain.Tip
import com.fx.api.exception.TipException
import com.fx.api.exception.errorcode.TipBaseErrorCode
import com.fx.global.domain.DeviceType
import org.springframework.stereotype.Service

@Service
class TipCommandQueryService(
    private val tipPersistencePort: TipPersistencePort
) : TipCommandUseCase, TipQueryUseCase {

    override fun saveTip(tipCommand: TipCommand): Boolean {
        tipPersistencePort.saveTip(Tip.createTip(tipCommand))
        return true
    }

    override fun deleteTip(tipId: String): Boolean {
        tipPersistencePort.deleteById(tipId)
        return true
    }

    override fun getTips(deviceType: DeviceType): List<Tip> {
        val tips = tipPersistencePort.getTips(deviceType)
        if (tips.isEmpty()) {
            throw TipException(TipBaseErrorCode.TIP_NOT_FOUND)
        }
        return tips
    }

}