package com.mobilekey.backend

import com.mobilekey.backend.auth.dto.LoginRequest
import com.mobilekey.backend.auth.dto.PasswordResetConfirm
import com.mobilekey.backend.auth.dto.PasswordResetRequest
import com.mobilekey.backend.auth.dto.RefreshRequest
import com.mobilekey.backend.auth.dto.RegisterRequest
import com.mobilekey.backend.auth.dto.TokenResponse
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

    fun register(body: Map<String, Any?>): ResponseEntity<MessageResponse> =
        restTemplate.postForEntity("/api/auth/register", body, MessageResponse::class.java)

    fun registerExpectError(request: RegisterRequest): ResponseEntity<MessageResponse> =
        restTemplate.postForEntity("/api/auth/register", request, MessageResponse::class.java)

    // --- Login ---

    fun login(request: LoginRequest): ResponseEntity<TokenResponse> =
        restTemplate.postForEntity("/api/auth/login", request, TokenResponse::class.java)

    fun login(body: Map<String, Any?>): ResponseEntity<MessageResponse> =
        restTemplate.postForEntity("/api/auth/login", body, MessageResponse::class.java)

    fun loginExpectError(request: LoginRequest): ResponseEntity<MessageResponse> =
        restTemplate.postForEntity("/api/auth/login", request, MessageResponse::class.java)

    // --- Refresh ---

    fun refresh(request: RefreshRequest): ResponseEntity<TokenResponse> =
        restTemplate.postForEntity("/api/auth/refresh", request, TokenResponse::class.java)

    fun refresh(body: Map<String, Any?>): ResponseEntity<MessageResponse> =
        restTemplate.postForEntity("/api/auth/refresh", body, MessageResponse::class.java)

    fun refreshExpectError(request: RefreshRequest): ResponseEntity<MessageResponse> =
        restTemplate.postForEntity("/api/auth/refresh", request, MessageResponse::class.java)

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

    fun requestPasswordReset(body: Map<String, Any?>): ResponseEntity<MessageResponse> =
        restTemplate.postForEntity("/api/auth/password-reset/request", body, MessageResponse::class.java)

    fun confirmPasswordReset(request: PasswordResetConfirm): ResponseEntity<MessageResponse> =
        restTemplate.postForEntity("/api/auth/password-reset/confirm", request, MessageResponse::class.java)

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
