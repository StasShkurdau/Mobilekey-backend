package com.mobilekey.backend.common.config

import com.mobilekey.backend.auth.config.JwtProperties
import com.mobilekey.backend.auth.config.PasswordResetProperties
import com.mobilekey.backend.file.config.FileProperties
import com.mobilekey.backend.file.config.MinioProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@EnableScheduling
@EnableConfigurationProperties(
    JwtProperties::class,
    PasswordResetProperties::class,
    MinioProperties::class,
    FileProperties::class,
)
class AppConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
