package com.mobilekey.backend.file.service

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class FileCleanupScheduler(private val fileService: FileService) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.MINUTES)
    fun cleanupExpiredTempFiles() {
        runBlocking {
            try {
                fileService.cleanupExpiredTemp()
                log.info("Temp file cleanup completed")
            } catch (e: Exception) {
                log.error("Temp file cleanup failed", e)
            }
        }
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    fun cleanupDeletedFiles() {
        runBlocking {
            try {
                fileService.cleanupExpiredDeleted()
                log.info("Deleted file cleanup completed")
            } catch (e: Exception) {
                log.error("Deleted file cleanup failed", e)
            }
        }
    }
}
