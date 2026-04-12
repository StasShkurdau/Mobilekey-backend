package com.mobilekey.backend.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 255)
    val login: String,

    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    @field:Size(min = 6, max = 255)
    val password: String,
)

data class LoginRequest(
    @field:NotBlank
    val login: String,

    @field:NotBlank
    val password: String,
)

data class RefreshRequest(
    @field:NotBlank
    val refreshToken: String,
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
)

data class PasswordResetRequest(
    @field:NotBlank
    @field:Email
    val email: String,
)

data class PasswordResetConfirm(
    @field:NotBlank
    val code: String,

    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    @field:Size(min = 6, max = 255)
    val newPassword: String,
)
