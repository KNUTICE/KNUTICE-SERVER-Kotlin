package com.fx.crawler.domain.batch

sealed interface JobLaunchOutcome {

    data class Launched(val jobExecutionId: Long) : JobLaunchOutcome

    data class Rejected(val reason: String) : JobLaunchOutcome

}
