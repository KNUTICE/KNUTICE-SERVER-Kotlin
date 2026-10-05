package com.fx.api.application.service.tip

import com.fx.api.application.port.`in`.tip.TipCommandUseCase
import com.fx.api.application.port.`in`.tip.TipQueryUseCase
import com.fx.api.application.port.`in`.tip.dto.TipSaveCommand
import com.fx.api.application.port.out.tip.TipPersistencePort
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
