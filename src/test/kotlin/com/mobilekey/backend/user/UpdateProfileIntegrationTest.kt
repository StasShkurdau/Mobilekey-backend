package com.mobilekey.backend.user

import com.mobilekey.backend.IntegrationTestBase
import com.mobilekey.backend.auth.dto.LoginRequest
import com.mobilekey.backend.auth.dto.RegisterRequest
import com.mobilekey.backend.auth.dto.TokenResponse
import com.mobilekey.backend.user.dto.UpdateUserRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class UpdateProfileIntegrationTest : IntegrationTestBase() {

    private lateinit var tokens: TokenResponse

    @BeforeEach
    fun registerUser() {
        tokens = authClient.register(RegisterRequest("testuser", "test@example.com", "password123")).body!!
    }

    @Test
    fun `update login returns 200 with updated login`() {
        val response = authClient.updateProfile(tokens.accessToken, UpdateUserRequest(login = "newlogin", newPassword = null))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("newlogin", response.body?.login)
    }

    @Test
    fun `update login is reflected in profile`() {
        authClient.updateProfile(tokens.accessToken, UpdateUserRequest(login = "newlogin", newPassword = null))

        val profile = authClient.getProfile(tokens.accessToken)

        assertEquals("newlogin", profile.body?.login)
    }

    @Test
    fun `update login returns 400 when login is already taken`() {
        authClient.register(RegisterRequest("otheruser", "other@example.com", "password123"))

        val response = authClient.updateProfileExpectError(tokens.accessToken, UpdateUserRequest(login = "otheruser", newPassword = null))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("user.login_already_taken", response.body?.code)
    }

    @Test
    fun `update login returns 400 when login is too short`() {
        val response = authClient.updateProfileExpectError(tokens.accessToken, UpdateUserRequest(login = "ab", newPassword = null))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("validation_error", response.body?.code)
    }

    @Test
    fun `update password allows login with new password`() {
        authClient.updateProfile(tokens.accessToken, UpdateUserRequest(login = null, newPassword = "newpassword456"))

        val loginResponse = authClient.login(LoginRequest("testuser", "newpassword456"))

        assertEquals(HttpStatus.OK, loginResponse.statusCode)
    }

    @Test
    fun `update password invalidates old password`() {
        authClient.updateProfile(tokens.accessToken, UpdateUserRequest(login = null, newPassword = "newpassword456"))

        val loginResponse = authClient.loginExpectError(LoginRequest("testuser", "password123"))

        assertEquals(HttpStatus.BAD_REQUEST, loginResponse.statusCode)
        assertEquals("auth.invalid_credentials", loginResponse.body?.code)
    }

    @Test
    fun `update login and password at the same time`() {
        authClient.updateProfile(tokens.accessToken, UpdateUserRequest(login = "updateduser", newPassword = "newpassword456"))

        val loginResponse = authClient.login(LoginRequest("updateduser", "newpassword456"))

        assertEquals(HttpStatus.OK, loginResponse.statusCode)
    }

    @Test
    fun `update with null login and null password does not change anything`() {
        authClient.updateProfile(tokens.accessToken, UpdateUserRequest(login = null, newPassword = null))

        val profile = authClient.getProfile(tokens.accessToken)

        assertEquals("testuser", profile.body?.login)
    }

    @Test
    fun `update password returns 400 when password is too short`() {
        val response = authClient.updateProfileExpectError(tokens.accessToken, UpdateUserRequest(login = null, newPassword = "short"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("validation_error", response.body?.code)
    }
}
