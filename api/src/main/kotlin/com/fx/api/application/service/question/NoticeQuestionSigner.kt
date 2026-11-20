package com.fx.api.application.service.question

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64
import java.util.HexFormat
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * 공지 질문의 서명 · 해시 (HMAC-SHA256).
 *
 * - [deviceKey]: fcmToken 원문 대신 저장 · 비교하는 기기 키 (64자 hex)
 * - [finishToken]: prepare 가 준 질문에만, 같은 사람이, 정해진 시간 안에 답변을 저장하게 묶는 서명
 *
 * 비밀값은 `notice-question.secret` (기본은 JWT 서명 키). 용도마다 앞말(`device:` · `finish:`)을 달리해 섞이지 않게 한다.
 */
@Component
class NoticeQuestionSigner(
    @Value("\${notice-question.secret}") secret: String,
) {

    private val key = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), ALGORITHM)

    init {
        require(secret.length >= MIN_SECRET_LENGTH) {
            "notice-question.secret 은 ${MIN_SECRET_LENGTH}자 이상이어야 합니다."
        }
    }

    fun deviceKey(fcmToken: String): String =
        HexFormat.of().formatHex(hmac("device:$fcmToken"))

    /** `만료 epoch 초.서명(base64url)` */
    fun finishToken(questionId: Long, askerKey: String, expiresAt: Instant): String {
        val expires = expiresAt.epochSecond
        return "$expires.${encode(hmac(finishPayload(questionId, askerKey, expires)))}"
    }

    fun verifyFinishToken(token: String, questionId: Long, askerKey: String, now: Instant): Boolean {
        val expires = token.substringBefore('.', "").toLongOrNull() ?: return false
        val signature = token.substringAfter('.', "")
        if (signature.isEmpty() || now.epochSecond > expires) {
            return false
        }
        val expected = encode(hmac(finishPayload(questionId, askerKey, expires)))
        return MessageDigest.isEqual(expected.toByteArray(), signature.toByteArray())
    }

    private fun finishPayload(questionId: Long, askerKey: String, expires: Long) =
        "finish:$questionId:$askerKey:$expires"

    private fun hmac(payload: String): ByteArray =
        Mac.getInstance(ALGORITHM).apply {
            init(key)
        }.doFinal(payload.toByteArray(Charsets.UTF_8))

    private fun encode(bytes: ByteArray): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    companion object {
        private const val ALGORITHM = "HmacSHA256"
        private const val MIN_SECRET_LENGTH = 32
    }

}
