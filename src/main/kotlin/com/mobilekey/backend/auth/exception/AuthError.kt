package com.mobilekey.backend.auth.exception

import com.mobilekey.backend.common.exception.ApiError
import org.springframework.http.HttpStatus

enum class AuthError(
    override val code: String,
    override val message: String,
    override val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST,
) : ApiError {
    EMAIL_ALREADY_TAKEN("auth.email_already_taken", "Email already taken"),
    INVALID_CREDENTIALS("auth.invalid_credentials", "Invalid credentials"),
    INVALID_REFRESH_TOKEN("auth.invalid_refresh_token", "Invalid refresh token", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED("auth.refresh_token_expired", "Refresh token is expired or revoked", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND("auth.user_not_found", "User not found"),
    RESET_CODE_EXPIRED("auth.reset_code_expired", "Reset code expired or not found"),
    INVALID_RESET_CODE("auth.invalid_reset_code", "Invalid reset code"),
}
