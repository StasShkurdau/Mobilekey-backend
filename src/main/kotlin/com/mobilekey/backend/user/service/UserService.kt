package com.mobilekey.backend.user.service

import com.mobilekey.backend.common.exception.ApiException
import com.mobilekey.backend.user.dto.UpdateUserRequest
import com.mobilekey.backend.user.dto.UserResponse
import com.mobilekey.backend.user.entity.User
import com.mobilekey.backend.user.exception.UserError
import com.mobilekey.backend.user.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(private val userRepository: UserRepository) {

    fun findById(id: UUID): User {
        return userRepository.findById(id)
            ?: throw ApiException(UserError.USER_NOT_FOUND)
    }

    fun getProfile(userId: UUID): UserResponse {
        val user = findById(userId)
        return user.toResponse()
    }

    fun updateProfile(userId: UUID, request: UpdateUserRequest): UserResponse {
        val user = findById(userId)

        request.login?.let { login ->
            if (login != user.login && userRepository.existsByLogin(login)) {
                throw ApiException(UserError.LOGIN_ALREADY_TAKEN)
            }
        }

        request.email?.let { email ->
            if (email != user.email && userRepository.existsByEmail(email)) {
                throw ApiException(UserError.EMAIL_ALREADY_TAKEN)
            }
        }

        val updated = user.copy(
            login = request.login ?: user.login,
            email = request.email ?: user.email,
        )

        return userRepository.update(updated).toResponse()
    }

    private fun User.toResponse() = UserResponse(
        id = id,
        login = login,
        email = email,
    )
}
