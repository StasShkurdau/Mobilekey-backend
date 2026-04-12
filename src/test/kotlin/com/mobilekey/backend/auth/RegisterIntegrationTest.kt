package com.mobilekey.backend.auth

import com.mobilekey.backend.IntegrationTestBase
import com.mobilekey.backend.auth.dto.RegisterRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class RegisterIntegrationTest : IntegrationTestBase() {

    @Test
    fun `register returns 201 with tokens for valid request`() {
        val response = authClient.register(RegisterRequest("newuser", "new@example.com", "password123"))

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body?.accessToken)
        assertNotNull(response.body?.refreshToken)
    }

    @Test
    fun `register returns 400 when login is already taken`() {
        authClient.register(RegisterRequest("duplicate", "first@example.com", "password123"))

        val response = authClient.registerExpectError(RegisterRequest("duplicate", "second@example.com", "password123"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Login already taken", response.body?.message)
    }

    @Test
    fun `register returns 400 when email is already taken`() {
        authClient.register(RegisterRequest("user1", "same@example.com", "password123"))

        val response = authClient.registerExpectError(RegisterRequest("user2", "same@example.com", "password123"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Email already taken", response.body?.message)
    }

    @Test
    fun `register returns 400 when login is blank`() {
        val response = authClient.register(mapOf("login" to "", "email" to "test@example.com", "password" to "password123"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `register returns 400 when login is too short`() {
        val response = authClient.registerExpectError(RegisterRequest("ab", "test@example.com", "password123"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `register returns 400 when email is invalid`() {
        val response = authClient.register(mapOf("login" to "validuser", "email" to "not-an-email", "password" to "password123"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `register returns 400 when password is too short`() {
        val response = authClient.registerExpectError(RegisterRequest("validuser", "test@example.com", "12345"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `register returns tokens that can be used for authenticated requests`() {
        val tokens = authClient.register(RegisterRequest("authuser", "auth@example.com", "password123")).body!!

        val profileResponse = authClient.getProfile(tokens.accessToken)

        assertEquals(HttpStatus.OK, profileResponse.statusCode)
        assertEquals("authuser", profileResponse.body?.login)
    }
}
