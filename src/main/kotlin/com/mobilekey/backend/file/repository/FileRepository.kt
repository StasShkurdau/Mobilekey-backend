package com.mobilekey.backend.file.repository

import com.mobilekey.backend.common.config.JooqExecutor
import com.mobilekey.backend.file.entity.FileEntity
import com.mobilekey.backend.file.entity.FileStatus
import com.mobilekey.backend.generated.tables.references.FILE
import com.mobilekey.backend.generated.tables.records.FileRecord
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
class FileRepository(private val jooqExecutor: JooqExecutor) {

    suspend fun save(file: FileEntity): FileEntity = jooqExecutor.execute {
        insertInto(FILE)
            .set(FILE.ID, file.id)
            .set(FILE.NAME, file.name)
            .set(FILE.CONTENT_TYPE, file.contentType)
            .set(FILE.SIZE, file.size)
            .set(FILE.PATH, file.path)
            .set(FILE.STATUS, file.status.name)
            .set(FILE.CREATED_AT, file.createdAt)
            .execute()
        file
    }

    suspend fun findById(id: UUID): FileEntity? = jooqExecutor.execute {
        selectFrom(FILE)
            .where(FILE.ID.eq(id))
            .fetchOne { it.toEntity() }
    }

    suspend fun updatePathAndStatus(id: UUID, path: String, status: FileStatus) = jooqExecutor.execute {
        update(FILE)
            .set(FILE.PATH, path)
            .set(FILE.STATUS, status.name)
            .where(FILE.ID.eq(id))
            .execute()
    }

    suspend fun deleteById(id: UUID) = jooqExecutor.execute {
        deleteFrom(FILE)
            .where(FILE.ID.eq(id))
            .execute()
    }

    suspend fun findExpiredByStatus(status: FileStatus, cutoff: LocalDateTime): List<FileEntity> = jooqExecutor.execute {
        selectFrom(FILE)
            .where(FILE.STATUS.eq(status.name))
            .and(FILE.CREATED_AT.lt(cutoff))
            .fetch { it.toEntity() }
    }

    private fun FileRecord.toEntity(): FileEntity = FileEntity(
        id = id,
        name = name,
        contentType = contentType,
        size = size,
        path = path,
        status = FileStatus.valueOf(status),
        createdAt = createdAt,
    )
}
