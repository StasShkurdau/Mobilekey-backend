package com.mobilekey.backend.auth

import com.mobilekey.backend.IntegrationTestBase
import com.mobilekey.backend.auth.dto.RefreshRequest
import com.mobilekey.backend.auth.dto.RegisterRequest
import com.mobilekey.backend.auth.dto.TokenResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class RefreshTokenIntegrationTest : IntegrationTestBase() {

    private fun registerAndGetTokens(): TokenResponse =
        authClient.register(RegisterRequest("testuser", "test@example.com", "password123")).body!!

    @Test
    fun `refresh returns 200 with new tokens for valid refresh token`() {
        val tokens = registerAndGetTokens()

        val response = authClient.refresh(RefreshRequest(tokens.refreshToken))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body?.accessToken)
        assertNotNull(response.body?.refreshToken)
    }

    @Test
    fun `refresh returns new access token that works for authenticated requests`() {
        val tokens = registerAndGetTokens()
        val newTokens = authClient.refresh(RefreshRequest(tokens.refreshToken)).body!!

        val profileResponse = authClient.getProfile(newTokens.accessToken)

        assertEquals(HttpStatus.OK, profileResponse.statusCode)
    }

    @Test
    fun `refresh invalidates old refresh token`() {
        val tokens = registerAndGetTokens()
        val oldRefreshToken = tokens.refreshToken

        // Delay to ensure the new token gets a different iat timestamp
        Thread.sleep(1000)

        val firstRefresh = authClient.refresh(RefreshRequest(oldRefreshToken))
        assertEquals(HttpStatus.OK, firstRefresh.statusCode)

        val reuse = authClient.refreshExpectError(RefreshRequest(oldRefreshToken))

        assertEquals(HttpStatus.BAD_REQUEST, reuse.statusCode)
    }

    @Test
    fun `refresh returns 400 for invalid JWT`() {
        val response = authClient.refreshExpectError(RefreshRequest("not.a.valid.jwt"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Invalid refresh token", response.body?.message)
    }

    @Test
    fun `refresh returns 400 for empty refresh token`() {
        val response = authClient.refresh(mapOf("refreshToken" to ""))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `refresh returns 400 after logout`() {
        val tokens = registerAndGetTokens()

        authClient.logout(tokens.accessToken)

        val response = authClient.refreshExpectError(RefreshRequest(tokens.refreshToken))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `new refresh token from refresh can be used again`() {
        val tokens = registerAndGetTokens()
        val newTokens = authClient.refresh(RefreshRequest(tokens.refreshToken)).body!!

        val response = authClient.refresh(RefreshRequest(newTokens.refreshToken))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body?.accessToken)
    }
}
