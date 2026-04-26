package com.mobilekey.backend.user.dto

import jakarta.validation.constraints.Size
import java.util.UUID

data class UserResponse(
    val id: UUID,
    val login: String,
    val email: String,
    val avatarId: UUID?,
)

data class UpdateUserRequest(
    @field:Size(min = 3, max = 255)
    val login: String? = null,

    @field:Size(min = 8, max = 255)
    val newPassword: String? = null,

    val avatarId: UUID? = null,
)
