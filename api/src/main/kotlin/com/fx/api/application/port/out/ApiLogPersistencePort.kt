package com.fx.api.application.port.out

import com.fx.global.domain.ApiLog

interface ApiLogPersistencePort {

    fun save(apiLog: ApiLog)

}