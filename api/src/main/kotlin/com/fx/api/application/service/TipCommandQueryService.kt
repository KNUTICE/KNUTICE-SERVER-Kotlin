package com.fx.api.application.service

import com.fx.api.application.port.`in`.TipCommandUseCase
import com.fx.api.application.port.`in`.TipQueryUseCase
import com.fx.api.application.port.`in`.dto.TipSaveCommand
import com.fx.api.application.port.out.TipPersistencePort
import com.fx.api.domain.Tip
import com.fx.api.exception.TipException
import com.fx.api.exception.errorcode.TipErrorCode
import com.fx.common.domain.DeviceType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TipCommandQueryService(
    private val tipPersistencePort: TipPersistencePort,
) : TipCommandUseCase, TipQueryUseCase {

    @Transactional
    override fun saveTip(tipSaveCommand: TipSaveCommand): Boolean {
        tipPersistencePort.save(Tip(tipSaveCommand.title, tipSaveCommand.url, tipSaveCommand.deviceType))
        return true
    }

    /** 없는 팁을 지워도 성공으로 본다. */
    @Transactional
    override fun deleteTip(tipId: String): Boolean {
        tipId.toLongOrNull()?.let(tipPersistencePort::deleteById)
        return true
    }

    override fun getTips(deviceType: DeviceType): List<Tip> {
        val tips = tipPersistencePort.findAllByDeviceType(deviceType)
        if (tips.isEmpty()) {
            throw TipException(TipErrorCode.TIP_NOT_FOUND)
        }
        return tips
    }

}
