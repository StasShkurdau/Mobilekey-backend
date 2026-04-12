package com.mobilekey.backend.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size
import java.util.UUID

data class UserResponse(
    val id: UUID,
    val login: String,
    val email: String,
)

data class UpdateUserRequest(
    @field:Size(min = 3, max = 255)
    val login: String?,

    @field:Email
    val email: String?,
)
