package com.mobilekey.backend.outbox.avatar.processor

import com.mobilekey.backend.outbox.avatar.repository.UpdateAvatarRequestRepository
import com.mobilekey.backend.outbox.avatar.service.UpdateAvatarRequestExecutor
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
@ConditionalOnProperty("app.outbox.avatar.enabled", havingValue = "true", matchIfMissing = true)
class UpdateAvatarProcessor(
    private val repository: UpdateAvatarRequestRepository,
    private val executor: UpdateAvatarRequestExecutor,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    fun process() = runBlocking {
        val requests = repository.findUnprocessedBatch(BATCH_SIZE)
        for (request in requests) {
            try {
                executor.execute(request)
            } catch (e: Exception) {
                log.error("Failed to process UpdateAvatarRequest ${request.id}: ${e.message}")
            }
        }
    }

    companion object {
        private const val BATCH_SIZE = 1000
    }
}
