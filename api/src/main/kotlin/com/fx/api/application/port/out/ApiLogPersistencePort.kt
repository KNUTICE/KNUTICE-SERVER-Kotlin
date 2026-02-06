package com.fx.api.application.port.out

import com.fx.api.domain.ApiLog

interface ApiLogPersistencePort {

    fun save(apiLog: ApiLog)

}