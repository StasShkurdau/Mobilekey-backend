package com.mobilekey.backend.user.service

import com.mobilekey.backend.common.config.JooqExecutor
import com.mobilekey.backend.common.exception.ApiException
import com.mobilekey.backend.outbox.avatar.entity.UpdateAvatarRequestDto
import com.mobilekey.backend.outbox.avatar.service.UpdateAvatarRequestExecutor
import com.mobilekey.backend.user.dto.UpdateUserRequest
import com.mobilekey.backend.user.dto.UserResponse
import com.mobilekey.backend.user.entity.User
import com.mobilekey.backend.user.exception.UserError
import com.mobilekey.backend.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val userAvatarService: UserAvatarService,
    private val updateAvatarRequestExecutor: UpdateAvatarRequestExecutor,
    private val jooq: JooqExecutor,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun findById(id: UUID): User {
        return userRepository.findById(id)
            ?: throw ApiException(UserError.USER_NOT_FOUND)
    }

    suspend fun getProfile(userId: UUID): UserResponse {
        val user = findById(userId)
        return user.toResponse()
    }

    suspend fun updateProfile(userId: UUID, request: UpdateUserRequest): UserResponse {
        val user = findById(userId)

        asserLoginAlreadyUsed(request.login, user)

        val updatedUserProfile = user.copy(
            login = request.login ?: user.login,
            password = request.newPassword?.let { passwordEncoder.encode(it) } ?: user.password,
            avatarId = request.avatarId,
        )

        val avatarRequests = userAvatarService.prepareRequests(user.avatarId, request.avatarId)

        jooq.transaction { ctx ->
            userAvatarService.saveRequestsWithContext(ctx, avatarRequests)
            userRepository.updateWithContext(ctx, updatedUserProfile)
        }

        // Best-effort sync execution after commit so the file moves are visible immediately.
        // If anything fails, the scheduled processor will retry — the request is durably stored.
        avatarRequests.forEach { avatarRequest ->
            tryToUpdateFiles(avatarRequest)
        }

        return updatedUserProfile.toResponse()
    }

    private suspend fun tryToUpdateFiles(avatarRequest: UpdateAvatarRequestDto) {
        try {
            updateAvatarRequestExecutor.execute(avatarRequest)
        } catch (e: Exception) {
            log.error("Sync execution of UpdateAvatarRequest ${avatarRequest.id} failed; will retry async: ${e.message}")
        }
    }

    private suspend fun asserLoginAlreadyUsed(newLogin: String?, user: User) {
        newLogin?.let { login ->
            if (login != user.login && userRepository.existsByLogin(login)) {
                throw ApiException(UserError.LOGIN_ALREADY_TAKEN)
            }
        }
    }

    private fun User.toResponse() = UserResponse(
        id = id,
        login = login,
        email = email,
        avatarId = avatarId,
    )
}
