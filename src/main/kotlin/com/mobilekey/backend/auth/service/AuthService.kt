package com.mobilekey.backend.auth.service

import com.mobilekey.backend.auth.dto.LoginRequest
import com.mobilekey.backend.auth.dto.RegisterRequest
import com.mobilekey.backend.auth.dto.TokenResponse
import com.mobilekey.backend.auth.security.JwtService
import com.mobilekey.backend.auth.security.RefreshTokenService
import com.mobilekey.backend.common.util.UuidGenerator
import com.mobilekey.backend.user.entity.User
import com.mobilekey.backend.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val refreshTokenService: RefreshTokenService,
    private val uuidGenerator: UuidGenerator,
) {

    fun register(request: RegisterRequest): TokenResponse {
        if (userRepository.existsByLogin(request.login)) {
            throw IllegalArgumentException("Login already taken")
        }
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already taken")
        }

        val user = User(
            id = uuidGenerator.generate(),
            login = request.login,
            email = request.email,
            password = passwordEncoder.encode(request.password),
        )

        userRepository.save(user)
        return generateTokens(user)
    }

    fun login(request: LoginRequest): TokenResponse {
        val user = userRepository.findByLogin(request.login)
            ?: throw IllegalArgumentException("Invalid credentials")

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw IllegalArgumentException("Invalid credentials")
        }

        return generateTokens(user)
    }

    fun refresh(refreshToken: String): TokenResponse {
        val userId = jwtService.validateRefreshToken(refreshToken)
            ?: throw IllegalArgumentException("Invalid refresh token")

        if (!refreshTokenService.isValid(userId, refreshToken)) {
            throw IllegalArgumentException("Refresh token is expired or revoked")
        }

        val user = userRepository.findById(userId)
            ?: throw IllegalArgumentException("User not found")

        refreshTokenService.delete(userId)
        return generateTokens(user)
    }

    fun logout(userId: UUID) {
        refreshTokenService.delete(userId)
    }

    private fun generateTokens(user: User): TokenResponse {
        val accessToken = jwtService.generateAccessToken(user.id)
        val refreshToken = jwtService.generateRefreshToken(user.id)
        refreshTokenService.save(user.id, refreshToken)
        return TokenResponse(accessToken = accessToken, refreshToken = refreshToken)
    }
}
