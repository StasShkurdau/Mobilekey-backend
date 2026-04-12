package com.mobilekey.backend.common.config

import com.mobilekey.backend.auth.config.JwtProperties
import com.mobilekey.backend.auth.config.PasswordResetProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@EnableConfigurationProperties(JwtProperties::class, PasswordResetProperties::class)
class AppConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
