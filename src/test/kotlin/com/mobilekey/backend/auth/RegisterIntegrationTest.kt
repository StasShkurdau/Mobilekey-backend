package com.mobilekey.backend.auth

import com.mobilekey.backend.IntegrationTestBase
import com.mobilekey.backend.auth.dto.RegisterRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class RegisterIntegrationTest : IntegrationTestBase() {

    @Test
    fun `register returns 201 with tokens for valid request`() {
        val response = authClient.register(RegisterRequest("new@example.com", "password123"))

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body?.accessToken)
        assertNotNull(response.body?.refreshToken)
    }

    @Test
    fun `register returns 400 when email is already taken`() {
        authClient.register(RegisterRequest("same@example.com", "password123"))

        val response = authClient.registerExpectError(RegisterRequest("same@example.com", "password123"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("auth.email_already_taken", response.body?.code)
    }

    @Test
    fun `register returns 400 when email is invalid`() {
        val response = authClient.register(mapOf("email" to "not-an-email", "password" to "password123"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("validation_error", response.body?.code)
    }

    @Test
    fun `register returns 400 when password is too short`() {
        val response = authClient.registerExpectError(RegisterRequest("test@example.com", "12345"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("validation_error", response.body?.code)
    }

    @Test
    fun `register returns tokens that can be used for authenticated requests`() {
        val tokens = authClient.register(RegisterRequest("auth@example.com", "password123")).body!!

        val profileResponse = authClient.getProfile(tokens.accessToken)

        assertEquals(HttpStatus.OK, profileResponse.statusCode)
        assertNull(profileResponse.body?.login)
        assertEquals("auth@example.com", profileResponse.body?.email)
    }
}
