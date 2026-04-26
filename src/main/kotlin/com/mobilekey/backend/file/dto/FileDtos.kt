package com.mobilekey.backend.file.dto

import java.time.LocalDateTime
import java.util.UUID

data class FileResponse(
    val id: UUID,
    val name: String,
    val contentType: String,
    val size: Long,
    val status: String,
    val createdAt: LocalDateTime,
)
