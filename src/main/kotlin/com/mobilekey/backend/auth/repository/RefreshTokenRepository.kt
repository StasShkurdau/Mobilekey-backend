package com.mobilekey.backend.auth.repository

import com.mobilekey.backend.auth.config.JwtProperties
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration
import java.util.UUID

@Repository
class RefreshTokenRepository(
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val jwtProperties: JwtProperties,
) {

    companion object {
        private const val PREFIX = "refresh_token:"
    }

    suspend fun save(userId: UUID, refreshToken: String) {
        redisTemplate.opsForValue().set(
            PREFIX + userId,
            refreshToken,
            Duration.ofMillis(jwtProperties.refreshExpirationMs),
        ).awaitSingle()
    }

    suspend fun find(userId: UUID): String? {
        return redisTemplate.opsForValue().get(PREFIX + userId).awaitSingleOrNull()
    }

    suspend fun delete(userId: UUID) {
        redisTemplate.delete(PREFIX + userId).awaitSingle()
    }
}
