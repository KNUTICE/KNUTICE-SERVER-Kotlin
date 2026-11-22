package com.fx.crawler.support

import com.fx.common.application.port.out.WebhookPort
import com.fx.common.domain.SlackMessage
import com.fx.common.domain.catalog.TopicView
import com.fx.crawler.application.port.out.MealPort
import com.fx.common.domain.notice.EMBEDDING_DIMENSIONS
import com.fx.crawler.application.port.out.NoticeCrawlPort
import com.fx.crawler.application.port.out.NoticeEmbeddingPort
import com.fx.crawler.application.port.out.NoticeImagePort
import com.fx.crawler.application.port.out.NoticeSummaryPort
import com.fx.crawler.application.port.out.PushPort
import com.fx.crawler.domain.crawl.CrawledNotice
import com.fx.crawler.domain.crawl.NoticeDetail
import com.fx.crawler.domain.embedding.EmbeddingInput
import com.fx.crawler.domain.embedding.EmbeddingRejectedException
import com.fx.crawler.domain.embedding.EmbeddingUnavailableException
import com.fx.crawler.domain.meal.Meal
import com.fx.crawler.domain.push.PushMessage
import com.fx.crawler.domain.push.PushTarget
import com.fx.crawler.domain.summary.SummaryRateLimitedException
import com.fx.readingroom.application.port.out.ReadingRoomRemotePort
import com.fx.readingroom.domain.ReadingRoom
import com.fx.readingroom.domain.ReadingRoomSeat
import com.fx.readingroom.domain.ReadingRoomStatus
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 외부 시스템(FCM · 학교 사이트 · AI · 열람실 사이트 · Slack) 대신 쓰는 가짜 어댑터.
 * 발송 Step 이 파티션을 병렬로 실행하므로 기록은 스레드 안전한 컬렉션에 담는다.
 */
@TestConfiguration(proxyBeanMethods = false)
class FakeExternalAdapters {

    @Bean @Primary fun fakePushPort() =
        FakePushPort()
    @Bean @Primary fun fakeNoticeCrawlPort() =
        FakeNoticeCrawlPort()
    @Bean @Primary fun fakeMealPort() =
        FakeMealPort()
    @Bean @Primary fun fakeNoticeSummaryPort() =
        FakeNoticeSummaryPort()
    @Bean @Primary fun fakeNoticeEmbeddingPort() =
        FakeNoticeEmbeddingPort()
    @Bean @Primary fun fakeNoticeImagePort() =
        FakeNoticeImagePort()
    @Bean @Primary fun fakeReadingRoomRemotePort() =
        FakeReadingRoomRemotePort()
    @Bean @Primary fun fakeWebhookPort() =
        FakeWebhookPort()

}

class FakePushPort : PushPort {

    /** @property message 사일런트 푸시면 null */
    data class Sent(val tokens: List<String>, val message: PushMessage?)

    val sent = ConcurrentLinkedQueue<Sent>()

    /** 등록이 풀린 것으로 응답할 토큰 */
    val invalidTokens: MutableSet<String> = ConcurrentHashMap.newKeySet()

    override fun send(targets: List<PushTarget>, message: PushMessage): List<Long> {
        sent += Sent(targets.map {
            it.token
        }, message)
        return invalidIds(targets)
    }

    override fun sendSilent(targets: List<PushTarget>): List<Long> {
        sent += Sent(targets.map {
            it.token
        }, null)
        return invalidIds(targets)
    }

    private fun invalidIds(targets: List<PushTarget>) =
        targets.filter {
            it.token in invalidTokens
        }.map {
            it.fcmTokenId
        }

    fun reset() {
        sent.clear()
        invalidTokens.clear()
    }

}

class FakeNoticeCrawlPort : NoticeCrawlPort {

    /** 토픽 이름별 목록. 값이 [failure] 면 그 토픽 크롤링이 실패한다 */
    val lists = ConcurrentHashMap<String, List<Row>>()
    val details = ConcurrentHashMap<String, NoticeDetail>()
    val failingTopics: MutableSet<String> = ConcurrentHashMap.newKeySet()

    data class Row(val nttId: Long, val title: String)

    override fun fetchNoticeList(topic: TopicView): List<CrawledNotice> {
        check(topic.name !in failingTopics) {
            "${topic.name} 게시판 응답 없음"
        }
        return lists[topic.name].orEmpty().map {
            CrawledNotice(
                nttId = it.nttId,
                topic = topic,
                title = it.title,
                department = "학사팀",
                contentUrl = contentUrl(it.nttId),
                registrationDate = LocalDate.of(2026, 9, 30),
                isAttachment = false,
            )
        }
    }

    override fun fetchNoticeDetail(contentUrl: String): NoticeDetail =
        details[contentUrl] ?: error("상세 페이지 응답 없음: $contentUrl")

    fun reset() {
        lists.clear()
        details.clear()
        failingTopics.clear()
    }

    companion object {
        fun contentUrl(nttId: Long) =
            "https://www.ut.ac.kr/notice?nttId=$nttId"
    }

}

class FakeMealPort : MealPort {

    val meals = ConcurrentHashMap<Int, Meal>()

    override fun fetchMeal(topic: TopicView, date: LocalDate): Meal? =
        meals[topic.code]?.copy(mealDate = date)

    fun reset() =
        meals.clear()

}

class FakeNoticeSummaryPort : NoticeSummaryPort {

    val failingContents: MutableSet<String> = ConcurrentHashMap.newKeySet()

    /** 켜면 모든 요청이 호출 한도에 걸린다 */
    @Volatile var rateLimited = false

    override fun summarize(content: String): String {
        if (rateLimited) {
            throw SummaryRateLimitedException("429 RESOURCE_EXHAUSTED")
        }
        check(content !in failingContents) {
            "AI 응답 없음"
        }
        return "요약: $content"
    }

    fun reset() {
        failingContents.clear()
        rateLimited = false
    }

}

/** 입력마다 한 칸만 1 인 단위 벡터를 돌려준다. 텍스트는 글자 수 칸, 이미지는 마지막 칸. */
class FakeNoticeEmbeddingPort : NoticeEmbeddingPort {

    override val modelTag: String = "fake-embedding@$EMBEDDING_DIMENSIONS"

    /** 받은 요청 (본문 조각 묶음 하나, 이미지 한 장, 제목 하나가 각각 요청 하나) */
    val calls = ConcurrentLinkedQueue<List<EmbeddingInput>>()

    /** 이 글이 들어간 텍스트를 받으면 실패한다 */
    val failingTexts: MutableSet<String> = ConcurrentHashMap.newKeySet()

    /** 이 형식의 이미지는 모델이 받지 않는다 (400) */
    val rejectedMimeTypes: MutableSet<String> = ConcurrentHashMap.newKeySet()

    /** 켜면 모든 요청이 호출 한도에 걸린다 */
    @Volatile var rateLimited = false

    override fun checkAvailable() {
        if (rateLimited) {
            throw EmbeddingUnavailableException("429 RESOURCE_EXHAUSTED")
        }
    }

    override fun embed(inputs: List<EmbeddingInput>): List<FloatArray> {
        checkAvailable()
        calls += inputs
        inputs.forEach { input ->
            when (input) {
                is EmbeddingInput.Text -> check(failingTexts.none { input.text.contains(it) }) {
                    "임베딩 응답 없음"
                }
                is EmbeddingInput.Image -> if (input.mimeType in rejectedMimeTypes) {
                    throw EmbeddingRejectedException("400 INVALID_ARGUMENT")
                }
            }
        }
        return inputs.map { input ->
            val index = when (input) {
                is EmbeddingInput.Text -> input.text.length % (EMBEDDING_DIMENSIONS - 1)
                is EmbeddingInput.Image -> EMBEDDING_DIMENSIONS - 1
            }
            FloatArray(EMBEDDING_DIMENSIONS).also {
                it[index] = 1f
            }
        }
    }

    fun reset() {
        calls.clear()
        failingTexts.clear()
        rejectedMimeTypes.clear()
        rateLimited = false
    }

}

/** 상세 페이지 주소 → 본문 이미지 주소, 이미지 주소 → 이미지. 없는 이미지는 건너뛴 이미지(null)로 본다. */
class FakeNoticeImagePort : NoticeImagePort {

    val pages = ConcurrentHashMap<String, List<String>>()
    val images = ConcurrentHashMap<String, EmbeddingInput.Image>()

    /** 이 상세 페이지를 읽으면 학교 사이트 오류가 난다 */
    val failingPages: MutableSet<String> = ConcurrentHashMap.newKeySet()

    /** 읽으려 한 상세 페이지 (학교 사이트 요청) */
    val visitedPages = ConcurrentLinkedQueue<String>()

    /** 켜면 학교 사이트 장애로 요청을 멈춘 상태가 된다 */
    @Volatile var paused = false

    override fun checkAvailable() {
        if (paused) {
            throw EmbeddingUnavailableException("학교 사이트 장애로 요청하지 않음")
        }
    }

    override fun findImageUrls(contentUrl: String): List<String> {
        checkAvailable()
        visitedPages += contentUrl
        check(contentUrl !in failingPages) {
            "학교 사이트 오류 500 - $contentUrl"
        }
        return pages[contentUrl].orEmpty()
    }

    override fun fetchImage(imageUrl: String): EmbeddingInput.Image? =
        images[imageUrl]

    fun reset() {
        pages.clear()
        images.clear()
        failingPages.clear()
        visitedPages.clear()
        paused = false
    }

}

class FakeReadingRoomRemotePort : ReadingRoomRemotePort {

    /** 열람실별 빈 좌석 번호. 없는 열람실은 조회에 실패한다 */
    val availableSeats = ConcurrentHashMap<ReadingRoom, Set<Int>>()

    override fun getCsrfToken(): String =
        "csrf"

    override fun getReadingRoomStatus(): List<ReadingRoomStatus> =
        emptyList()

    override fun getReadingRoomSeats(readingRoom: ReadingRoom, csrfToken: String): List<ReadingRoomSeat> {
        val available = availableSeats[readingRoom] ?: error("$readingRoom 조회 실패")
        return (1..20).map {
            ReadingRoomSeat(
                roomId = readingRoom,
                seatNumber = it,
                row = 1,
                column = it,
                isAvailable = it in available,
                returnAt = LocalDate.of(2026, 9, 30).atStartOfDay(),
            )
        }
    }

    fun reset() =
        availableSeats.clear()

}

class FakeWebhookPort : WebhookPort {

    val messages = ConcurrentLinkedQueue<SlackMessage>()

    override fun notifySlack(slackMessage: SlackMessage) {
        messages += slackMessage
    }

    fun reset() =
        messages.clear()

}
