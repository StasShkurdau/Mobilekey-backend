package com.mobilekey.backend.auth.repository

import com.mobilekey.backend.auth.config.PasswordResetProperties
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class PasswordResetRepository(
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val passwordResetProperties: PasswordResetProperties,
) {

    companion object {
        private const val PREFIX = "password_reset:"
    }

    suspend fun save(email: String, code: String) {
        redisTemplate.opsForValue().set(
            PREFIX + email,
            code,
            Duration.ofMillis(passwordResetProperties.expirationMs),
        ).awaitSingle()
    }

    suspend fun find(email: String): String? {
        return redisTemplate.opsForValue().get(PREFIX + email).awaitSingleOrNull()
    }

    suspend fun delete(email: String) {
        redisTemplate.delete(PREFIX + email).awaitSingle()
    }
}
