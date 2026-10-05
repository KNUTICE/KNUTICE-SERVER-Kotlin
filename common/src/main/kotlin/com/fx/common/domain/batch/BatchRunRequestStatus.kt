package com.fx.common.domain.batch

enum class BatchRunRequestStatus {

    /** 요청됨. 폴러가 다음 확인 때 실행한다. */
    REQUESTED,

    /** Job 을 실행했다. */
    LAUNCHED,

    /** 실행하지 않았다 (모르는 Job, 잘못된 파라미터, 같은 작업이 실행 중 등). */
    REJECTED,

}
