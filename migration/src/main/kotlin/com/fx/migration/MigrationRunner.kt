package com.fx.migration

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.ExitCodeGenerator
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/** 기동하면 바로 이관하고, 건수 검증에 실패하면 종료 코드 1 을 돌려준다. */
@Component
@ConditionalOnProperty(prefix = "migration", name = ["run-on-startup"], havingValue = "true", matchIfMissing = true)
class MigrationRunner(
    private val migrationService: MigrationService,
) : ApplicationRunner, ExitCodeGenerator {

    @Volatile
    private var exitCode = 0

    override fun run(args: ApplicationArguments) {
        exitCode = if (migrationService.migrate().successful) 0 else 1
    }

    override fun getExitCode(): Int = exitCode

}
