package com.mobilekey.backend.user.exception

import com.mobilekey.backend.common.exception.ApiError
import org.springframework.http.HttpStatus

enum class UserError(
    override val code: String,
    override val message: String,
    override val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST,
) : ApiError {
    USER_NOT_FOUND("user.user_not_found", "User not found"),
    LOGIN_ALREADY_TAKEN("user.login_already_taken", "Login already taken"),
    EMAIL_ALREADY_TAKEN("user.email_already_taken", "Email already taken"),
}
