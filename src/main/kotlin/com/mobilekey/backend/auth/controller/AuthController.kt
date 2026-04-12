package com.mobilekey.backend.auth.controller

import com.mobilekey.backend.auth.dto.*
import com.mobilekey.backend.auth.service.AuthService
import com.mobilekey.backend.auth.service.PasswordResetService
import com.mobilekey.backend.common.dto.MessageResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val passwordResetService: PasswordResetService,
) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody request: RegisterRequest): TokenResponse {
        return authService.register(request)
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): TokenResponse {
        return authService.login(request)
    }

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshRequest): TokenResponse {
        return authService.refresh(request.refreshToken)
    }

    @PostMapping("/logout")
    fun logout(principal: Principal): MessageResponse {
        authService.logout(UUID.fromString(principal.name))
        return MessageResponse("Logged out successfully")
    }

    @PostMapping("/password-reset/request")
    fun requestPasswordReset(@Valid @RequestBody request: PasswordResetRequest): MessageResponse {
        passwordResetService.requestReset(request.email)
        return MessageResponse("If the email exists, a reset code has been sent")
    }

    @PostMapping("/password-reset/confirm")
    fun confirmPasswordReset(@Valid @RequestBody request: PasswordResetConfirm): MessageResponse {
        passwordResetService.confirmReset(request.email, request.code, request.newPassword)
        return MessageResponse("Password has been reset successfully")
    }
}
