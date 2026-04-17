package com.mobilekey.backend.auth.service

import com.mobilekey.backend.auth.dto.LoginRequest
import com.mobilekey.backend.auth.dto.RegisterRequest
import com.mobilekey.backend.auth.dto.TokenResponse
import com.mobilekey.backend.auth.exception.AuthError
import com.mobilekey.backend.auth.security.JwtService
import com.mobilekey.backend.auth.security.RefreshTokenService
import com.mobilekey.backend.common.exception.ApiException
import com.mobilekey.backend.common.util.UuidGenerator
import com.mobilekey.backend.user.entity.User
import com.mobilekey.backend.user.repository.UserRepository
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.security.core.context.ReactiveSecurityContextHolder
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

    suspend fun register(request: RegisterRequest): TokenResponse {
        if (userRepository.existsByLogin(request.login)) {
            throw ApiException(AuthError.LOGIN_ALREADY_TAKEN)
        }
        if (userRepository.existsByEmail(request.email)) {
            throw ApiException(AuthError.EMAIL_ALREADY_TAKEN)
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

    suspend fun login(request: LoginRequest): TokenResponse {
        val user = userRepository.findByLogin(request.login)
            ?: throw ApiException(AuthError.INVALID_CREDENTIALS)

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw ApiException(AuthError.INVALID_CREDENTIALS)
        }

        return generateTokens(user)
    }

    suspend fun refresh(refreshToken: String): TokenResponse {
        val userId = jwtService.validateRefreshToken(refreshToken)
            ?: throw ApiException(AuthError.INVALID_REFRESH_TOKEN)

        if (!refreshTokenService.isValid(userId, refreshToken)) {
            throw ApiException(AuthError.REFRESH_TOKEN_EXPIRED)
        }

        val user = userRepository.findById(userId)
            ?: throw ApiException(AuthError.USER_NOT_FOUND)

        refreshTokenService.delete(userId)

        return generateTokens(user)
    }

    suspend fun logout() {
        val userId = currentUserId()

        refreshTokenService.delete(userId)
    }

    suspend fun currentUserId(): UUID {
        val authentication = ReactiveSecurityContextHolder.getContext()
            .map { it.authentication }
            .awaitSingle()

        return authentication.principal as UUID
    }

    private suspend fun generateTokens(user: User): TokenResponse {
        val accessToken = jwtService.generateAccessToken(user.id)
        val refreshToken = jwtService.generateRefreshToken(user.id)

        refreshTokenService.save(user.id, refreshToken)

        return TokenResponse(accessToken = accessToken, refreshToken = refreshToken)
    }
}
