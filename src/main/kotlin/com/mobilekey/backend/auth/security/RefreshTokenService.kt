package com.mobilekey.backend.auth.security

import com.mobilekey.backend.auth.repository.RefreshTokenRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
) {

    suspend fun save(userId: UUID, refreshToken: String) {
        refreshTokenRepository.save(userId, refreshToken)
    }

    suspend fun delete(userId: UUID) {
        refreshTokenRepository.delete(userId)
    }

    suspend fun isValid(userId: UUID, refreshToken: String): Boolean {
        val storedToken = refreshTokenRepository.find(userId)
        
        return (storedToken != null) && (storedToken == refreshToken)
    }
}
