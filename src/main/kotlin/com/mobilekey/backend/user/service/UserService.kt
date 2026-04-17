package com.mobilekey.backend.user.service

import com.mobilekey.backend.common.exception.ApiException
import com.mobilekey.backend.user.dto.UpdateUserRequest
import com.mobilekey.backend.user.dto.UserResponse
import com.mobilekey.backend.user.entity.User
import com.mobilekey.backend.user.exception.UserError
import com.mobilekey.backend.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {

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

        val newLogin = request.login

        asserLoginAlreadyUsed(newLogin, user)

        val updated = user.copy(
            login = newLogin ?: user.login,
            password = request.newPassword?.let { passwordEncoder.encode(it) } ?: user.password,
        )

        return userRepository.update(updated).toResponse()
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
    )
}
