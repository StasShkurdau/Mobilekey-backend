package com.mobilekey.backend.user.service

import com.mobilekey.backend.common.util.UuidGenerator
import com.mobilekey.backend.file.entity.FileStatus
import com.mobilekey.backend.file.repository.FileRepository
import com.mobilekey.backend.outbox.avatar.entity.UpdateAvatarRequestDto
import com.mobilekey.backend.outbox.avatar.repository.UpdateAvatarRequestRepository
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class UserAvatarService(
    private val fileRepository: FileRepository,
    private val updateAvatarRequestRepository: UpdateAvatarRequestRepository,
    private val uuidGenerator: UuidGenerator,
) {

    suspend fun prepareRequests(oldAvatarId: UUID?, newAvatarId: UUID?): List<UpdateAvatarRequestDto> {
        if (oldAvatarId == newAvatarId) return emptyList()

        val requests = mutableListOf<UpdateAvatarRequestDto>()

        oldAvatarId?.let { id ->
            requests.add(buildRequest(id, "deleted/$id", FileStatus.DELETED))
        }

        newAvatarId?.let { id ->
            val file = fileRepository.findById(id) ?: return@let
            if (file.status == FileStatus.TEMP) {
                requests.add(buildRequest(id, "private/user/avatar/$id", FileStatus.ATTACHED))
            }
        }

        return requests
    }

    fun saveRequestsWithContext(ctx: DSLContext, requests: List<UpdateAvatarRequestDto>) {
        requests.forEach { updateAvatarRequestRepository.saveInTx(ctx, it) }
    }

    private fun buildRequest(fileId: UUID, targetPath: String, newStatus: FileStatus): UpdateAvatarRequestDto {
        return UpdateAvatarRequestDto(
            id = uuidGenerator.generate(),
            fileId = fileId,
            targetPath = targetPath,
            newStatus = newStatus,
            createdAt = LocalDateTime.now(),
        )
    }
}
