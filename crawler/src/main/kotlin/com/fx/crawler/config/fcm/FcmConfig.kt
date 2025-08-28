package com.fx.crawler.config.fcm

import com.fx.crawler.appllication.service.NoticeCrawlService
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.io.IOException

@Configuration
class FcmConfig(
    @Value("\${firebase.secret.key.path}")
    private val fcmKeyPath: String
) {

    private val log = LoggerFactory.getLogger(FcmConfig::class.java)

    @PostConstruct
    fun init() {
        try {
            val refreshToken = ClassPathResource(fcmKeyPath).inputStream
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(refreshToken))
                .build()
            FirebaseApp.initializeApp(options)
            log.info("FCM Setting Complete!!!")
        } catch (e: IOException) {
            throw RuntimeException("Failed to initialize FCM: ${e.message}", e)
        }
    }
}