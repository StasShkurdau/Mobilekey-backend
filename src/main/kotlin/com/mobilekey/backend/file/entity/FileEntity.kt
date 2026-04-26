package com.mobilekey.backend.file.entity

import java.time.LocalDateTime
import java.util.UUID

data class FileEntity(
    val id: UUID,
    val name: String,
    val contentType: String,
    val size: Long,
    val path: String,
    val status: FileStatus,
    val createdAt: LocalDateTime,
)

enum class FileStatus {
    TEMP,
    ATTACHED,
    DELETED,
}
