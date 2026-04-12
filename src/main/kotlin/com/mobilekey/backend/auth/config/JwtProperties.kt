package com.mobilekey.backend.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.jwt")
data class JwtProperties(
    val accessSecret: String,
    val refreshSecret: String,
    val accessExpirationMs: Long,
    val refreshExpirationMs: Long,
)
