package com.fx.crawler.application.port.out

import com.fx.crawler.domain.push.PushMessage
import com.fx.crawler.domain.push.PushTarget

/**
 * 푸시 발송. 발송은 되돌릴 수 없으므로 전송 오류로 예외를 던지지 않고, 결과만 돌려준다.
 * 반환값은 등록이 풀려(앱 삭제 등) 다시 보낼 수 없는 토큰의 ID 다.
 */
interface PushPort {

    fun send(targets: List<PushTarget>, message: PushMessage): List<Long>

    /** 표시하지 않는 iOS 백그라운드 푸시 (토큰 갱신 유도). */
    fun sendSilent(targets: List<PushTarget>): List<Long>

}
