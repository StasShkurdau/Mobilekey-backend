package com.mobilekey.backend.outbox.avatar.service

import com.mobilekey.backend.file.repository.FileRepository
import com.mobilekey.backend.file.service.FileService
import com.mobilekey.backend.outbox.avatar.entity.UpdateAvatarRequestDto
import com.mobilekey.backend.outbox.avatar.repository.UpdateAvatarRequestRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UpdateAvatarRequestExecutor(
    private val repository: UpdateAvatarRequestRepository,
    private val fileService: FileService,
    private val fileRepository: FileRepository,
) {

    suspend fun execute(request: UpdateAvatarRequestDto) {
        val file = fileRepository.findById(request.fileId)

        if (file != null && file.path != request.targetPath) {
            fileService.copyToPath(file.path, request.targetPath)
            fileRepository.updatePathAndStatus(request.fileId, request.targetPath, request.newStatus)
            fileService.safeRemoveFromStorage(file.path)
        }

        repository.markProcessed(request.id, LocalDateTime.now())
    }
}
