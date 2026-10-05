package com.fx.readingroom

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

/**
 * reading-room 은 실행 모듈이 아니므로 슬라이스 테스트용 부트 설정을 테스트 소스에 둔다.
 * 좌석 알림 조회가 common 의 FCM 토큰 엔티티와 조인하므로 엔티티는 `com.fx` 전체를 스캔한다.
 */
@SpringBootApplication
@EnableJpaAuditing
@EntityScan(basePackages = ["com.fx"])
class ReadingRoomTestApplication
