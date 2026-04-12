package com.mobilekey.backend.auth.security

import com.mobilekey.backend.auth.config.JwtProperties
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.TimeUnit

@Service
class RefreshTokenService(
    private val redisTemplate: StringRedisTemplate,
    private val jwtProperties: JwtProperties,
) {

    companion object {
        private const val PREFIX = "refresh_token:"
    }

    fun save(userId: UUID, refreshToken: String) {
        val key = PREFIX + userId
        redisTemplate.opsForValue().set(
            key,
            refreshToken,
            jwtProperties.refreshExpirationMs,
            TimeUnit.MILLISECONDS,
        )
    }

    fun get(userId: UUID): String? {
        return redisTemplate.opsForValue().get(PREFIX + userId)
    }

    fun delete(userId: UUID) {
        redisTemplate.delete(PREFIX + userId)
    }

    fun isValid(userId: UUID, refreshToken: String): Boolean {
        val stored = get(userId)
        return stored != null && stored == refreshToken
    }
}
