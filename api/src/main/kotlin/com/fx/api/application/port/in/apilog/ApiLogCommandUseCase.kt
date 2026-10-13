package com.fx.api.application.port.`in`.apilog

import com.fx.api.application.port.`in`.apilog.dto.ApiLogCommand

interface ApiLogCommandUseCase {

    /** 요청 기록 묶음을 한 트랜잭션으로 저장한다. 토큰은 토큰 ID 로, 토픽 파라미터는 토픽 코드 · 이름으로 바꾼다. */
    fun saveAll(commands: List<ApiLogCommand>)

}
