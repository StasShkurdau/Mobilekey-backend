package com.mobilekey.backend.user.entity

import java.util.UUID

data class User(
    val id: UUID,
    val login: String,
    val email: String,
    val password: String,
)
