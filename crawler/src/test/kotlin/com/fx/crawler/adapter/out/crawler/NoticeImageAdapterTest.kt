package com.fx.crawler.adapter.out.crawler

import com.fx.crawler.config.CrawlerProperties
import com.fx.crawler.domain.embedding.EmbeddingUnavailableException
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.util.unit.DataSize
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import java.io.IOException
import java.net.InetSocketAddress
import java.time.Clock
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

/** 실제 HTTP 서버(127.0.0.1)를 학교 사이트 대신 띄워 요청 · 리다이렉트 · 크기 제한을 확인한다. */
class NoticeImageAdapterTest {

    private val requested = CopyOnWriteArrayList<String>()
    private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
        createContext("/") {
            handle(it)
        }
        // 느린 응답이 다른 요청을 막지 않도록 요청마다 스레드를 쓴다
        executor = Executors.newCachedThreadPool()
        start()
    }
    private val base = "http://127.0.0.1:${server.address.port}"

    /**
     * 경로마다 남은 횟수만큼 요청을 서버로 보내기 전에 끊긴 연결(IOException)로 만든다.
     * JDK HttpClient 는 서버가 닫은 연결에서 GET 을 안에서 한 번 더 보내므로, 어댑터의 재시도만 시험하려고 요청 단계에서 끊는다.
     */
    private val droppedRequests = ConcurrentHashMap<String, Int>()
    private val dropped = CopyOnWriteArrayList<String>()

    private val adapter = NoticeImageAdapter(
        RestClient.builder().requestInterceptor { request, body, execution ->
            val path = request.uri.rawPath
            val left = droppedRequests[path] ?: 0
            if (left > 0) {
                droppedRequests[path] = left - 1
                dropped += path
                throw IOException("closed")
            }
            execution.execute(request, body)
        },
        CrawlerProperties(
            embedding = CrawlerProperties.Embedding(
                allowedImageHost = "127.0.0.1",
                minImageSize = DataSize.ofBytes(100),
                maxImageSize = DataSize.ofBytes(1000),
                siteRequestInterval = Duration.ZERO,
                siteTimeout = Duration.ofSeconds(5),
                imageTimeout = Duration.ofSeconds(1),
            ),
        ),
        Clock.systemUTC(),
    )

    @AfterEach
    fun stop() {
        server.stop(0)
    }

    @Test
    fun `상세 페이지를 세션 ID 를 뗀 주소로 읽어 본문 이미지 주소를 돌려준다`() {
        val imageUrls = adapter.findImageUrls("$base/page;jsessionid=ABC123?nttId=1")

        assertThat(requested).containsExactly("/page?nttId=1")
        // 파서가 http 를 https 로 바꾼다 (학교 사이트는 둘 다 같은 파일을 준다)
        assertThat(imageUrls).containsExactly(
            "https://127.0.0.1:${server.address.port}/a.png",
            "https://other.example.com/x.png",
        )
    }

    @Test
    fun `이미지를 읽고 형식은 파일 앞부분으로 판단한다`() {
        val image = adapter.fetchImage("$base/a.png")

        assertThat(image).isNotNull
        assertThat(image!!.mimeType).isEqualTo("image/png")
        assertThat(image.bytes).hasSize(500)
    }

    @Test
    fun `너무 작거나 큰 이미지와 받지 않는 형식은 건너뛴다`() {
        assertThat(adapter.fetchImage("$base/tiny.png")).isNull()
        assertThat(adapter.fetchImage("$base/big.png")).isNull()
        assertThat(adapter.fetchImage("$base/a.gif")).isNull()
    }

    @Test
    fun `리다이렉트는 따라가지 않는다`() {
        assertThat(adapter.fetchImage("$base/redirect")).isNull()
        assertThat(requested).containsExactly("/redirect")
    }

    @Test
    fun `허용하지 않은 사이트에는 요청하지 않는다`() {
        assertThat(adapter.fetchImage("http://other.example.com/x.png")).isNull()
        assertThat(adapter.findImageUrls("https://other.example.com/page")).isEmpty()
        assertThat(adapter.fetchImage("file:///etc/passwd")).isNull()
        assertThat(requested).isEmpty()
    }

    @Test
    fun `상세 페이지는 네트워크 오류면 한 번 더 요청한다`() {
        droppedRequests["/page"] = 1

        assertThat(adapter.findImageUrls("$base/page")).hasSize(2)
        assertThat(dropped).containsExactly("/page")
        assertThat(requested).containsExactly("/page")
    }

    @Test
    fun `상세 페이지를 다시 요청해도 네트워크 오류면 예외로 알린다`() {
        droppedRequests["/page"] = 2

        assertThatThrownBy {
            adapter.findImageUrls("$base/page")
        }.isInstanceOf(ResourceAccessException::class.java)
        assertThat(dropped).containsExactly("/page", "/page")
        assertThat(requested).isEmpty()

        // 학교 사이트 장애로 보고 멈춘다. 그동안은 요청하지 않고 미루도록 알린다
        assertThatThrownBy {
            adapter.findImageUrls("$base/page")
        }.isInstanceOf(EmbeddingUnavailableException::class.java)
        assertThat(dropped).hasSize(2)
    }

    @Test
    fun `상세 페이지가 429 면 학교 사이트 장애로 보고 요청을 멈춘다`() {
        assertThatThrownBy {
            adapter.findImageUrls("$base/busy")
        }.isInstanceOf(IllegalStateException::class.java)
        assertThatThrownBy {
            adapter.findImageUrls("$base/page")
        }.isInstanceOf(EmbeddingUnavailableException::class.java)
        assertThatThrownBy {
            adapter.checkAvailable()
        }.isInstanceOf(EmbeddingUnavailableException::class.java)
        // 멈춘 동안에는 이미지도 받지 않는다
        assertThat(adapter.fetchImage("$base/a.png")).isNull()
        assertThat(requested).containsExactly("/busy")
    }

    @Test
    fun `상세 페이지가 리다이렉트 · 403 이면 이미지 없이 진행한다`() {
        // 로그인 전용 게시판은 로그인 화면으로 보낸다
        assertThat(adapter.findImageUrls("$base/redirect")).isEmpty()
        assertThat(adapter.findImageUrls("$base/forbidden")).isEmpty()
        // 장애로 보지 않으므로 계속 요청한다
        assertThat(adapter.findImageUrls("$base/page")).hasSize(2)
    }

    @Test
    fun `받지 못한 이미지는 다시 요청하지 않고 그 이미지만 건너뛴다`() {
        droppedRequests["/a.png"] = 1

        assertThat(adapter.fetchImage("$base/a.png")).isNull()
        assertThat(adapter.fetchImage("$base/error")).isNull()
        assertThat(dropped).containsExactly("/a.png")
        assertThat(requested).containsExactly("/error")
    }

    @Test
    fun `이미지 응답이 제한 시간보다 늦으면 그 이미지만 건너뛴다`() {
        val startedAt = System.nanoTime()

        assertThat(adapter.fetchImage("$base/slow.png")).isNull()
        // 서버는 3초 뒤에 응답하지만 1초 제한에 걸려 먼저 끝난다
        assertThat(Duration.ofNanos(System.nanoTime() - startedAt)).isLessThan(Duration.ofMillis(2500))
    }

    @Test
    fun `없는 페이지는 빈 목록, 상세 페이지 5xx 는 예외로 알리고 요청을 멈춘다`() {
        assertThat(adapter.findImageUrls("$base/missing")).isEmpty()
        assertThat(adapter.fetchImage("$base/missing")).isNull()
        assertThatThrownBy {
            adapter.findImageUrls("$base/error")
        }.isInstanceOf(IllegalStateException::class.java)
        assertThatThrownBy {
            adapter.findImageUrls("$base/page")
        }.isInstanceOf(EmbeddingUnavailableException::class.java)
        assertThat(requested).containsExactly("/missing", "/missing", "/error")
    }

    private fun handle(exchange: HttpExchange) {
        val uri = exchange.requestURI
        requested += uri.rawPath + (uri.rawQuery?.let { "?$it" } ?: "")
        when (uri.rawPath) {
            "/page" -> respond(
                exchange, 200,
                """<div class="bbs_detail_content"><img src="/a.png"><img src="http://other.example.com/x.png"></div>""".toByteArray(),
            )
            "/a.png" -> respond(exchange, 200, png(500))
            "/tiny.png" -> respond(exchange, 200, png(50))
            "/big.png" -> respond(exchange, 200, png(2000))
            "/a.gif" -> respond(exchange, 200, "GIF89a".toByteArray() + ByteArray(500))
            "/redirect" -> {
                exchange.responseHeaders.add("Location", "/a.png")
                respond(exchange, 302, null)
            }
            "/error" -> respond(exchange, 500, null)
            "/busy" -> respond(exchange, 429, null)
            "/forbidden" -> respond(exchange, 403, null)
            "/slow.png" -> {
                Thread.sleep(3000)
                respond(exchange, 200, png(500))
            }
            else -> respond(exchange, 404, null)
        }
    }

    private fun respond(exchange: HttpExchange, status: Int, body: ByteArray?) {
        exchange.sendResponseHeaders(status, body?.size?.toLong() ?: -1)
        body?.let {
            exchange.responseBody.write(it)
        }
        exchange.close()
    }

    private fun png(size: Int): ByteArray =
        byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A) + ByteArray(size - 8)

}
