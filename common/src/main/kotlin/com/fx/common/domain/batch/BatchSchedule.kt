package com.fx.common.domain.batch

import com.fx.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.DynamicUpdate
import java.time.LocalDateTime

/** 스케줄 키 최대 길이 — `schedule_key` 컬럼 길이와 같아야 한다. */
const val BATCH_SCHEDULE_KEY_MAX_LENGTH = 100

/** Job 이름 최대 길이 — `job_name` 컬럼 길이와 같아야 한다. */
const val BATCH_JOB_NAME_MAX_LENGTH = 100

/** Job 파라미터(JSON) 최대 길이 — `job_parameters` 컬럼 길이와 같아야 한다. */
const val BATCH_JOB_PARAMETERS_MAX_LENGTH = 1000

/** 스케줄 설명 최대 길이 — `description` 컬럼 길이와 같아야 한다. */
const val BATCH_SCHEDULE_DESCRIPTION_MAX_LENGTH = 200

/**
 * Job 자동 실행 정의.
 *
 * crawler 의 폴러가 1분마다 `enabled` 이고 [nextFireAt] 이 지난 스케줄을 찾아 실행한다.
 * 같은 Job 을 파라미터만 바꿔 여러 스케줄로 돌릴 수 있도록 [scheduleKey] 로 구분한다
 * (예: `notice-crawl-notice` · `notice-crawl-major` 는 둘 다 `noticeCrawlJob`).
 *
 * - [jobParameters] 는 문자열 값만 가진 JSON 객체다 (예: `{"topicType":"MAJOR"}`).
 * - [nextFireAt] 이 비어 있으면 아직 계산하지 않은 상태다. 폴러가 처음 볼 때 다음 발화 시각으로 채운다.
 * - 폴러는 조건부 UPDATE 로 발화를 선점하므로 [nextFireAt] · [lastFiredAt] 을 엔티티를 거치지 않고 바꾼다.
 *   관리자 수정은 바뀐 컬럼만 UPDATE 한다. 모든 컬럼을 쓰면 그사이 폴러가 넘긴 발화 시각을 읽어 둔 옛 값으로 되돌려 같은 발화를 한 번 더 실행한다.
 */
@Entity
@DynamicUpdate
@Table(
    name = "batch_schedule",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_batch_schedule_schedule_key", columnNames = ["schedule_key"]),
    ],
)
class BatchSchedule(
    scheduleKey: String,
    jobName: String,
    jobParameters: String,
    cron: String,
    enabled: Boolean,
    description: String,
) : BaseEntity() {

    @Column(name = "schedule_key", nullable = false, updatable = false, length = BATCH_SCHEDULE_KEY_MAX_LENGTH, comment = "스케줄 키")
    val scheduleKey: String = scheduleKey

    @Column(name = "job_name", nullable = false, updatable = false, length = BATCH_JOB_NAME_MAX_LENGTH, comment = "실행할 Job 이름")
    val jobName: String = jobName

    @Column(name = "job_parameters", nullable = false, length = BATCH_JOB_PARAMETERS_MAX_LENGTH, comment = "Job 파라미터 (JSON 객체)")
    var jobParameters: String = jobParameters
        protected set

    @Column(name = "cron", nullable = false, length = 100, comment = "Spring 6필드 cron (Asia/Seoul)")
    var cron: String = BatchCron.parse(cron).expression
        protected set

    @Column(name = "enabled", nullable = false, comment = "자동 실행 여부")
    var enabled: Boolean = enabled
        protected set

    @Column(name = "description", nullable = false, length = BATCH_SCHEDULE_DESCRIPTION_MAX_LENGTH, comment = "관리자 화면용 설명")
    var description: String = description
        protected set

    @Column(name = "next_fire_at", nullable = true, comment = "다음 발화 시각")
    var nextFireAt: LocalDateTime? = null
        protected set

    @Column(name = "last_fired_at", nullable = true, comment = "마지막 발화 시각")
    var lastFiredAt: LocalDateTime? = null
        protected set

    fun batchCron(): BatchCron =
        BatchCron.parse(cron)

    /** cron 을 바꾸고 다음 발화 시각을 다시 계산한다. */
    fun changeCron(cron: String, now: LocalDateTime) {
        val parsed = BatchCron.parse(cron)
        this.cron = parsed.expression
        if (enabled) {
            nextFireAt = parsed.next(now)
        }
    }

    /** 자동 실행을 켠다. 꺼져 있던 동안 지난 발화는 실행하지 않고 [now] 이후부터 다시 센다. */
    fun enable(now: LocalDateTime) {
        enabled = true
        nextFireAt = batchCron().next(now)
    }

    fun disable() {
        enabled = false
        nextFireAt = null
    }

    fun changeJobParameters(jobParameters: String) {
        this.jobParameters = jobParameters
    }

    fun changeDescription(description: String) {
        this.description = description
    }

}
