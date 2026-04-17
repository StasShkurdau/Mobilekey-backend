package com.mobilekey.backend.user.controller

import com.mobilekey.backend.auth.service.AuthService
import com.mobilekey.backend.user.dto.UpdateUserRequest
import com.mobilekey.backend.user.dto.UserResponse
import com.mobilekey.backend.user.service.UserService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val authService: AuthService,
) {

    @GetMapping("/me")
    suspend fun getProfile(): UserResponse {
        val userId = authService.currentUserId()

        return userService.getProfile(userId)
    }

    @PutMapping("/me")
    suspend fun updateProfile(@Valid @RequestBody request: UpdateUserRequest): UserResponse {
        val userId = authService.currentUserId()

        return userService.updateProfile(userId, request)
    }
}
