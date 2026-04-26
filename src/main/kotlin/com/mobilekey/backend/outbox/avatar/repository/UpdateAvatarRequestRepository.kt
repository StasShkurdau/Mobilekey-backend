package com.mobilekey.backend.outbox.avatar.repository

import com.mobilekey.backend.common.config.JooqExecutor
import com.mobilekey.backend.file.entity.FileStatus
import com.mobilekey.backend.generated.tables.records.UpdateAvatarRequestRecord
import com.mobilekey.backend.generated.tables.references.UPDATE_AVATAR_REQUEST
import com.mobilekey.backend.outbox.avatar.entity.UpdateAvatarRequestDto
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
class UpdateAvatarRequestRepository(private val jooq: JooqExecutor) {

    suspend fun findUnprocessedBatch(limit: Int): List<UpdateAvatarRequestDto> = jooq.execute {
        selectFrom(UPDATE_AVATAR_REQUEST)
            .where(UPDATE_AVATAR_REQUEST.PROCESSED_AT.isNull)
            .orderBy(UPDATE_AVATAR_REQUEST.CREATED_AT.asc())
            .limit(limit)
            .fetch { it.toEntity() }
    }

    fun saveInTx(ctx: DSLContext, request: UpdateAvatarRequestDto) {
        ctx.insertInto(UPDATE_AVATAR_REQUEST)
            .set(UPDATE_AVATAR_REQUEST.ID, request.id)
            .set(UPDATE_AVATAR_REQUEST.FILE_ID, request.fileId)
            .set(UPDATE_AVATAR_REQUEST.TARGET_PATH, request.targetPath)
            .set(UPDATE_AVATAR_REQUEST.NEW_STATUS, request.newStatus.name)
            .set(UPDATE_AVATAR_REQUEST.CREATED_AT, request.createdAt)
            .set(UPDATE_AVATAR_REQUEST.PROCESSED_AT, request.processedAt)
            .execute()
    }

    suspend fun markProcessed(id: UUID, processedAt: LocalDateTime) = jooq.execute {
        update(UPDATE_AVATAR_REQUEST)
            .set(UPDATE_AVATAR_REQUEST.PROCESSED_AT, processedAt)
            .where(UPDATE_AVATAR_REQUEST.ID.eq(id))
            .execute()
    }

    private fun UpdateAvatarRequestRecord.toEntity() = UpdateAvatarRequestDto(
        id = id,
        fileId = fileId,
        targetPath = targetPath,
        newStatus = FileStatus.valueOf(newStatus),
        createdAt = createdAt,
        processedAt = processedAt,
    )
}
