package com.fx.crawler.adapter.out.crawler

import com.fx.crawler.adapter.out.ai.RequestThrottle
import com.fx.crawler.application.port.out.NoticeImagePort
import com.fx.crawler.common.annotation.CrawlAdapter
import com.fx.crawler.config.CrawlerProperties
import com.fx.crawler.domain.embedding.EmbeddingInput
import com.fx.crawler.domain.embedding.EmbeddingUnavailableException
import com.fx.crawler.domain.embedding.ImageFormat
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import java.io.ByteArrayInputStream
import java.net.URI
import java.net.http.HttpClient
import java.time.Clock
import java.time.Duration
import java.time.Instant

/**
 * 임베딩용 공지 본문 이미지를 학교 사이트에서 읽는다.
 *
 * - [CrawlerProperties.Embedding.allowedImageHost] 와 그 하위 도메인만 요청한다. 관리자가 등록한 외부 링크나 본문의 외부 이미지는 읽지 않는다.
 * - 리다이렉트를 따라가지 않는다. 허용한 사이트가 다른 곳으로 보내도 그쪽으로 요청하지 않는다.
 * - 응답은 [CrawlerProperties.Embedding.maxImageSize] 까지만 읽는다. 더 크면 버린다.
 * - 크롤러가 저장한 링크의 세션 ID(`;jsessionid=…`)는 떼고 요청한다.
 * - 학교 사이트에 부담을 주지 않도록 요청 사이에 [CrawlerProperties.Embedding.siteRequestInterval] 간격을 둔다.
 *
 * 상세 페이지와 이미지는 실패를 다르게 다룬다 (2026-11-16 로컬 실행에서 학교 이미지 서버가 1.3MB 를 52~72초에 줌).
 * - 상세 페이지: [CrawlerProperties.Embedding.siteTimeout]. 네트워크 오류면 한 번 더 요청한다.
 *   - 404 · 3xx(로그인 전용 게시판) · 그 밖의 4xx 는 이미지 없이 진행한다 (빈 목록).
 *   - 429 · 5xx · 다시 해도 남는 네트워크 오류는 학교 사이트 장애로 본다. 그 공지는 예외로 실패시키고,
 *     [CrawlerProperties.Embedding.errorCooldown] 동안 학교 사이트에 요청하지 않는다 (그동안은 [EmbeddingUnavailableException] 으로 미룬다).
 * - 이미지: [CrawlerProperties.Embedding.imageTimeout] 으로 길게 기다리고, 그래도 받지 못하면(시간 초과 · 네트워크 · 4xx · 5xx) 그 이미지만 건너뛴다.
 *   같은 이미지는 다시 해도 느리므로, 이미지 하나 때문에 본문까지 임베딩하지 못하게 두지 않는다.
 */
@CrawlAdapter
class NoticeImageAdapter(
    restClientBuilder: RestClient.Builder,
    properties: CrawlerProperties,
    private val clock: Clock,
) : NoticeImagePort {

    private val log = LoggerFactory.getLogger(NoticeImageAdapter::class.java)
    private val embedding = properties.embedding
    private val throttle = RequestThrottle(embedding.siteRequestInterval, clock)
    private val allowedHost = embedding.allowedImageHost.lowercase()
    private val maxBytes = embedding.maxImageSize.toBytes()
    private val minBytes = embedding.minImageSize.toBytes()

    private val httpClient: HttpClient = HttpClient.newBuilder()
        // 학교 사이트는 HTTP/1.1 만 쓴다. HTTP/2 협상을 건너뛴다
        .version(HttpClient.Version.HTTP_1_1)
        .followRedirects(HttpClient.Redirect.NEVER)
        .connectTimeout(embedding.siteTimeout)
        .build()
    private val pageClient: RestClient = client(restClientBuilder.clone(), embedding.siteTimeout)
    private val imageClient: RestClient = client(restClientBuilder.clone(), embedding.imageTimeout)

    @Volatile
    private var pausedUntil: Instant = Instant.MIN

    override fun findImageUrls(contentUrl: String): List<String> {
        val url = withoutSessionId(contentUrl)
        if (!isAllowed(url)) {
            return emptyList()
        }
        checkNotPaused()

        val page = try {
            getPageWithRetry(url)
        } catch (e: ResourceAccessException) {
            pause("상세 페이지 네트워크 오류")
            throw e
        }
        return when {
            page.status in SUCCESS -> page.body?.let {
                NoticeHtmlParser.parseImageUrls(Jsoup.parse(ByteArrayInputStream(it), null, url))
            }.orEmpty()
            page.status == TOO_MANY_REQUESTS || page.status >= SERVER_ERROR -> {
                pause("학교 사이트 응답 ${page.status}")
                throw IllegalStateException("학교 사이트 응답 ${page.status} - $url")
            }
            else -> {
                log.info("상세 페이지 응답 {} 이라 이미지 없이 진행 - {}", page.status, url)
                emptyList()
            }
        }
    }

    override fun fetchImage(imageUrl: String): EmbeddingInput.Image? {
        val url = withoutSessionId(imageUrl)
        if (!isAllowed(url)) {
            log.info("허용하지 않은 사이트의 이미지라 건너뜀 - {}", url)
            return null
        }
        if (clock.instant().isBefore(pausedUntil)) {
            return null
        }
        val image = try {
            throttle.acquire()
            get(imageClient, url)
        } catch (e: RuntimeException) {
            log.warn("이미지를 받지 못해 건너뜀 - {} ({})", url, e.message)
            return null
        }
        if (image.status !in SUCCESS) {
            log.info("이미지 응답 {} 이라 건너뜀 - {}", image.status, url)
            return null
        }
        val bytes = image.body ?: return null
        if (bytes.size < minBytes) {
            return null
        }
        val format = ImageFormat.detect(bytes)
        if (format == null) {
            log.info("임베딩할 수 없는 이미지 형식이라 건너뜀 - {}", url)
            return null
        }
        return EmbeddingInput.Image(bytes, format.mimeType)
    }

    /** 상세 페이지를 읽는다. 네트워크 오류 · 시간 초과면 한 번 더 보낸다 (학교 사이트의 첫 응답이 늦을 때가 있다). */
    private fun getPageWithRetry(url: String): Fetched {
        repeat(PAGE_ATTEMPTS) { attempt ->
            throttle.acquire()
            try {
                return get(pageClient, url)
            } catch (e: ResourceAccessException) {
                if (attempt == PAGE_ATTEMPTS - 1) {
                    throw e
                }
                log.info("상세 페이지를 읽지 못해 다시 요청 ({}/{}) - {} ({})", attempt + 1, PAGE_ATTEMPTS, url, e.message)
            }
        }
        error("도달할 수 없는 코드")
    }

    /**
     * 응답 상태와 본문. 본문은 2xx 이고 [maxBytes] 이하일 때만 담는다.
     * @throws ResourceAccessException 네트워크 오류 · 시간 초과
     */
    private fun get(client: RestClient, url: String): Fetched =
        client.get()
            .uri(URI.create(url))
            .exchange { _, response ->
                val status = response.statusCode.value()
                if (status !in SUCCESS) {
                    return@exchange Fetched(status, null)
                }
                val bytes = response.body.readNBytes((maxBytes + 1).toInt())
                if (bytes.size > maxBytes) {
                    log.info("{} 바이트를 넘어 건너뜀 - {}", maxBytes, url)
                    Fetched(status, null)
                } else {
                    Fetched(status, bytes)
                }
            }

    private fun client(builder: RestClient.Builder, readTimeout: Duration): RestClient =
        builder
            .requestFactory(
                JdkClientHttpRequestFactory(httpClient).apply {
                    setReadTimeout(readTimeout)
                }
            )
            .build()

    private fun checkNotPaused() {
        val until = pausedUntil
        if (clock.instant().isBefore(until)) {
            throw EmbeddingUnavailableException("학교 사이트 장애로 $until 까지 요청하지 않습니다.")
        }
    }

    private fun pause(reason: String) {
        pausedUntil = clock.instant().plus(embedding.errorCooldown)
        log.warn("{} - {} 까지 학교 사이트에 요청하지 않음", reason, pausedUntil)
    }

    /** 허용한 도메인 · 하위 도메인의 http(s) 주소인지. */
    private fun isAllowed(url: String): Boolean {
        val uri = runCatching {
            URI.create(url)
        }.getOrNull() ?: return false
        val host = uri.host?.lowercase() ?: return false
        return uri.scheme in ALLOWED_SCHEMES && (host == allowedHost || host.endsWith(".$allowedHost"))
    }

    private fun withoutSessionId(url: String): String =
        url.replace(SESSION_ID, "")

    private class Fetched(val status: Int, val body: ByteArray?)

    companion object {
        private const val PAGE_ATTEMPTS = 2
        private const val TOO_MANY_REQUESTS = 429
        private const val SERVER_ERROR = 500
        private val SUCCESS = 200..299
        private val ALLOWED_SCHEMES = setOf("http", "https")
        private val SESSION_ID = Regex(";jsessionid=[^?#]*", RegexOption.IGNORE_CASE)
    }

}
