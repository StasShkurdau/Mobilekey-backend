package com.mobilekey.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.password-reset")
data class PasswordResetProperties(
    val expirationMs: Long,
)
