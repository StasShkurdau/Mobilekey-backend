package com.mobilekey.backend.service

import com.mobilekey.backend.dto.UpdateUserRequest
import com.mobilekey.backend.dto.UserResponse
import com.mobilekey.backend.entity.User
import com.mobilekey.backend.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(private val userRepository: UserRepository) {

    fun findById(id: UUID): User {
        return userRepository.findById(id)
            ?: throw IllegalArgumentException("User not found")
    }

    fun getProfile(userId: UUID): UserResponse {
        val user = findById(userId)
        return user.toResponse()
    }

    fun updateProfile(userId: UUID, request: UpdateUserRequest): UserResponse {
        val user = findById(userId)

        request.login?.let { login ->
            if (login != user.login && userRepository.existsByLogin(login)) {
                throw IllegalArgumentException("Login already taken")
            }
        }

        request.email?.let { email ->
            if (email != user.email && userRepository.existsByEmail(email)) {
                throw IllegalArgumentException("Email already taken")
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
