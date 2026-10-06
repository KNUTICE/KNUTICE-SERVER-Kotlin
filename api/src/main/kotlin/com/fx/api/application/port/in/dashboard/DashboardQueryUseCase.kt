package com.fx.api.application.port.`in`.dashboard

import com.fx.api.domain.Dashboard

interface DashboardQueryUseCase {

    fun getDashboard(): Dashboard

}
