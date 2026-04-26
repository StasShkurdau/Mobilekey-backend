package com.mobilekey.backend.file.service

import com.mobilekey.backend.common.exception.ApiException
import com.mobilekey.backend.common.util.UuidGenerator
import com.mobilekey.backend.file.config.FileProperties
import com.mobilekey.backend.file.config.MinioProperties
import com.mobilekey.backend.file.dto.FileResponse
import com.mobilekey.backend.file.entity.FileEntity
import com.mobilekey.backend.file.entity.FileStatus
import com.mobilekey.backend.file.exception.FileError
import com.mobilekey.backend.file.repository.FileRepository
import io.minio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import java.io.InputStream
import java.time.LocalDateTime
import java.util.UUID

@Service
class FileService(
    private val fileRepository: FileRepository,
    private val minioClient: MinioClient,
    private val minioProperties: MinioProperties,
    private val fileProperties: FileProperties,
    private val uuidGenerator: UuidGenerator,
) {

    suspend fun upload(filePart: FilePart): FileResponse {
        val contentType = filePart.headers().contentType?.toString()
            ?: throw ApiException(FileError.INVALID_FILE_TYPE)

        if (contentType !in fileProperties.allowedTypes) {
            throw ApiException(FileError.INVALID_FILE_TYPE)
        }

        val fileId = uuidGenerator.generate()

        val extension = extractExtension(filePart.filename())

        val path = "temp/${fileId}${extension}"

        val bytes = withContext(Dispatchers.IO) {
            filePart.content()
                .reduce { buffer1, buffer2 ->
                    buffer1.write(buffer2)
                    buffer1
                }
                .map { dataBuffer ->
                    val bytes = ByteArray(dataBuffer.readableByteCount())
                    dataBuffer.read(bytes)
                    bytes
                }
                .awaitSingle()!!
        }

        val size = bytes.size.toLong()

        if (size > fileProperties.maxSize) {
            throw ApiException(FileError.FILE_TOO_LARGE)
        }

        withContext(Dispatchers.IO) {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minioProperties.bucket)
                    .`object`(path)
                    .stream(bytes.inputStream(), size, -1)
                    .contentType(contentType)
                    .build()
            )
        }

        val fileEntity = FileEntity(
            id = fileId,
            name = filePart.filename(),
            contentType = contentType,
            size = size,
            path = path,
            status = FileStatus.TEMP,
            createdAt = LocalDateTime.now(),
        )

        fileRepository.save(fileEntity)
        return fileEntity.toResponse()
    }

    suspend fun download(fileId: UUID): Pair<FileEntity, InputStream> {
        val file = fileRepository.findById(fileId)
            ?: throw ApiException(FileError.FILE_NOT_FOUND)

        val inputStream = withContext(Dispatchers.IO) {
            minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(minioProperties.bucket)
                    .`object`(file.path)
                    .build()
            )
        }

        return file to inputStream
    }

    suspend fun cleanupExpiredTemp() {
        val cutoff = LocalDateTime.now().minusHours(fileProperties.tempExpirationHours)

        cleanupExpiredByStatus(FileStatus.TEMP, cutoff)
    }

    suspend fun cleanupExpiredDeleted() {
        val cutoff = LocalDateTime.now().minusHours(fileProperties.deletedExpirationHours)

        cleanupExpiredByStatus(FileStatus.DELETED, cutoff)
    }

    private suspend fun cleanupExpiredByStatus(status: FileStatus, cutoff: LocalDateTime) {
        val expiredFiles = fileRepository.findExpiredByStatus(status, cutoff)

        for (file in expiredFiles) {
            withContext(Dispatchers.IO) {
                try {
                    minioClient.removeObject(
                        RemoveObjectArgs.builder()
                            .bucket(minioProperties.bucket)
                            .`object`(file.path)
                            .build()
                    )
                } catch (_: Exception) {
                    // file may already be removed from storage
                }
            }
            fileRepository.deleteById(file.id)
        }
    }

    suspend fun copyToPath(sourcePath: String, targetPath: String) {
        withContext(Dispatchers.IO) {
            minioClient.copyObject(
                CopyObjectArgs.builder()
                    .bucket(minioProperties.bucket)
                    .`object`(targetPath)
                    .source(
                        CopySource.builder()
                            .bucket(minioProperties.bucket)
                            .`object`(sourcePath)
                            .build()
                    )
                    .build()
            )
        }
    }

    suspend fun safeRemoveFromStorage(path: String) {
        withContext(Dispatchers.IO) {
            try {
                minioClient.removeObject(
                    RemoveObjectArgs.builder()
                        .bucket(minioProperties.bucket)
                        .`object`(path)
                        .build()
                )
            } catch (_: Exception) {}
        }
    }

    private fun extractExtension(filename: String): String {
        val dotIndex = filename.lastIndexOf('.')
        return if (dotIndex >= 0) filename.substring(dotIndex) else ""
    }

    private fun FileEntity.toResponse() = FileResponse(
        id = id,
        name = name,
        contentType = contentType,
        size = size,
        status = status.name,
        createdAt = createdAt,
    )
}
