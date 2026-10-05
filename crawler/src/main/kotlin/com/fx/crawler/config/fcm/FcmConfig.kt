package com.fx.crawler.config.fcm

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource

@Configuration(proxyBeanMethods = false)
class FcmConfig {

    @Bean
    fun firebaseMessaging(@Value("\${firebase.secret.key.path}") keyPath: String): FirebaseMessaging {
        val app = FirebaseApp.getApps().firstOrNull()
            ?: ClassPathResource(keyPath).inputStream.use { key ->
                FirebaseApp.initializeApp(
                    FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(key))
                        .build()
                )
            }
        return FirebaseMessaging.getInstance(app)
    }

}
