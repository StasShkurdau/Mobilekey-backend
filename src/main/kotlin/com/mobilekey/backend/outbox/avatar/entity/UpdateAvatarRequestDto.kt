package com.mobilekey.backend.outbox.avatar.entity

import com.mobilekey.backend.file.entity.FileStatus
import java.time.LocalDateTime
import java.util.UUID

data class UpdateAvatarRequestDto(
    val id: UUID,
    val fileId: UUID,
    val targetPath: String,
    val newStatus: FileStatus,
    val createdAt: LocalDateTime,
    val processedAt: LocalDateTime? = null,
)
