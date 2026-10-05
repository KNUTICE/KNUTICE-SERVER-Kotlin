package com.fx.crawler.adapter.`in`.batch.support

import org.springframework.batch.infrastructure.support.transaction.ResourcelessTransactionManager
import org.springframework.transaction.PlatformTransactionManager

/**
 * 원격 호출(크롤링 · AI · 열람실 사이트)을 오래 기다리는 Step 용 트랜잭션 관리자.
 * Step 이 DB 커넥션을 잡고 기다리지 않도록 트랜잭션을 열지 않고, DB 저장은 영속성 어댑터가 짧은 트랜잭션으로 끝낸다.
 * (Step 실행 기록은 JobRepository 가 자체 트랜잭션으로 저장한다)
 */
object StepTransactions {

    val NONE: PlatformTransactionManager = ResourcelessTransactionManager()

}
