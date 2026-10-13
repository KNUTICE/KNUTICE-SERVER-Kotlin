package com.fx.api.application.port.out.apilog

import com.fx.api.domain.ApiLog

interface ApiLogPersistencePort {

    /** 묶어서 저장한다 (JDBC 배치 INSERT). */
    fun saveAll(apiLogs: List<ApiLog>)

}
