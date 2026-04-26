package com.mobilekey.backend.file.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.file")
data class FileProperties(
    val maxSize: Long,
    val allowedTypes: List<String>,
    val tempExpirationHours: Long,
    val deletedExpirationHours: Long,
)
