package com.mobilekey.backend

import com.mobilekey.backend.auth.dto.LoginRequest
import com.mobilekey.backend.auth.dto.PasswordResetConfirm
import com.mobilekey.backend.auth.dto.PasswordResetRequest
import com.mobilekey.backend.auth.dto.RefreshRequest
import com.mobilekey.backend.auth.dto.RegisterRequest
import com.mobilekey.backend.auth.dto.TokenResponse
import com.mobilekey.backend.common.dto.ErrorResponse
import com.mobilekey.backend.common.dto.MessageResponse
import com.mobilekey.backend.user.dto.UserResponse
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity

class AuthTestClient(private val restTemplate: TestRestTemplate) {

    // --- Register ---

    fun register(request: RegisterRequest): ResponseEntity<TokenResponse> =
        restTemplate.postForEntity("/api/auth/register", request, TokenResponse::class.java)

    fun register(body: Map<String, Any?>): ResponseEntity<ErrorResponse> =
        restTemplate.postForEntity("/api/auth/register", body, ErrorResponse::class.java)

    fun registerExpectError(request: RegisterRequest): ResponseEntity<ErrorResponse> =
        restTemplate.postForEntity("/api/auth/register", request, ErrorResponse::class.java)

    // --- Login ---

    fun login(request: LoginRequest): ResponseEntity<TokenResponse> =
        restTemplate.postForEntity("/api/auth/login", request, TokenResponse::class.java)

    fun login(body: Map<String, Any?>): ResponseEntity<ErrorResponse> =
        restTemplate.postForEntity("/api/auth/login", body, ErrorResponse::class.java)

    fun loginExpectError(request: LoginRequest): ResponseEntity<ErrorResponse> =
        restTemplate.postForEntity("/api/auth/login", request, ErrorResponse::class.java)

    // --- Refresh ---

    fun refresh(request: RefreshRequest): ResponseEntity<TokenResponse> =
        restTemplate.postForEntity("/api/auth/refresh", request, TokenResponse::class.java)

    fun refresh(body: Map<String, Any?>): ResponseEntity<ErrorResponse> =
        restTemplate.postForEntity("/api/auth/refresh", body, ErrorResponse::class.java)

    fun refreshExpectError(request: RefreshRequest): ResponseEntity<ErrorResponse> =
        restTemplate.postForEntity("/api/auth/refresh", request, ErrorResponse::class.java)

    // --- Logout ---

    fun logout(accessToken: String): ResponseEntity<MessageResponse> =
        restTemplate.postForEntity(
            "/api/auth/logout",
            HttpEntity<Void>(bearerHeaders(accessToken)),
            MessageResponse::class.java,
        )

    // --- Password Reset ---

    fun requestPasswordReset(request: PasswordResetRequest): ResponseEntity<MessageResponse> =
        restTemplate.postForEntity("/api/auth/password-reset/request", request, MessageResponse::class.java)

    fun requestPasswordReset(body: Map<String, Any?>): ResponseEntity<ErrorResponse> =
        restTemplate.postForEntity("/api/auth/password-reset/request", body, ErrorResponse::class.java)

    fun requestPasswordResetExpectError(request: PasswordResetRequest): ResponseEntity<ErrorResponse> =
        restTemplate.postForEntity("/api/auth/password-reset/request", request, ErrorResponse::class.java)

    fun confirmPasswordReset(request: PasswordResetConfirm): ResponseEntity<MessageResponse> =
        restTemplate.postForEntity("/api/auth/password-reset/confirm", request, MessageResponse::class.java)

    fun confirmPasswordResetExpectError(request: PasswordResetConfirm): ResponseEntity<ErrorResponse> =
        restTemplate.postForEntity("/api/auth/password-reset/confirm", request, ErrorResponse::class.java)

    // --- Profile ---

    fun getProfile(accessToken: String): ResponseEntity<UserResponse> =
        restTemplate.exchange(
            "/api/users/me",
            HttpMethod.GET,
            HttpEntity<Void>(bearerHeaders(accessToken)),
            UserResponse::class.java,
        )

    private fun bearerHeaders(token: String): HttpHeaders =
        HttpHeaders().apply { setBearerAuth(token) }
}
