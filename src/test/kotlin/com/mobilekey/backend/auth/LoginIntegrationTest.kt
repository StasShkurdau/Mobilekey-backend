package com.mobilekey.backend.auth

import com.mobilekey.backend.IntegrationTestBase
import com.mobilekey.backend.auth.dto.LoginRequest
import com.mobilekey.backend.auth.dto.RegisterRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class LoginIntegrationTest : IntegrationTestBase() {

    @BeforeEach
    fun registerUser() {
        authClient.register(RegisterRequest("testuser", "test@example.com", "password123"))
    }

    @Test
    fun `login returns 200 with tokens for valid credentials`() {
        val response = authClient.login(LoginRequest("testuser", "password123"))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body?.accessToken)
        assertNotNull(response.body?.refreshToken)
    }

    @Test
    fun `login returns 400 when user does not exist`() {
        val response = authClient.loginExpectError(LoginRequest("nonexistent", "password123"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Invalid credentials", response.body?.message)
    }

    @Test
    fun `login returns 400 when password is wrong`() {
        val response = authClient.loginExpectError(LoginRequest("testuser", "wrongpassword"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Invalid credentials", response.body?.message)
    }

    @Test
    fun `login returns same error for wrong user and wrong password`() {
        val wrongUser = authClient.loginExpectError(LoginRequest("nobody", "password123"))
        val wrongPassword = authClient.loginExpectError(LoginRequest("testuser", "wrong"))

        assertEquals(wrongUser.body?.message, wrongPassword.body?.message)
    }

    @Test
    fun `login returns 400 when login is blank`() {
        val response = authClient.login(mapOf("login" to "", "password" to "password123"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `login returns 400 when password is blank`() {
        val response = authClient.login(mapOf("login" to "testuser", "password" to ""))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `login returns tokens that work for authenticated endpoints`() {
        val tokens = authClient.login(LoginRequest("testuser", "password123")).body!!

        val profileResponse = authClient.getProfile(tokens.accessToken)

        assertEquals(HttpStatus.OK, profileResponse.statusCode)
        assertEquals("testuser", profileResponse.body?.login)
    }
}
