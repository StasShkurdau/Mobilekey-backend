package com.mobilekey.backend.user.controller

import com.mobilekey.backend.user.dto.UpdateUserRequest
import com.mobilekey.backend.user.dto.UserResponse
import com.mobilekey.backend.user.service.UserService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @GetMapping("/me")
    fun getProfile(principal: Principal): UserResponse {
        val userId = UUID.fromString(principal.name)
        return userService.getProfile(userId)
    }

    @PutMapping("/me")
    fun updateProfile(
        principal: Principal,
        @Valid @RequestBody request: UpdateUserRequest,
    ): UserResponse {
        val userId = UUID.fromString(principal.name)

        return userService.updateProfile(userId, request)
    }
}
