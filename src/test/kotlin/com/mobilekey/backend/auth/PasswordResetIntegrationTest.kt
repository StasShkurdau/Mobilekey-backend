package com.mobilekey.backend.auth

import com.mobilekey.backend.IntegrationTestBase
import com.mobilekey.backend.auth.dto.LoginRequest
import com.mobilekey.backend.auth.dto.PasswordResetConfirm
import com.mobilekey.backend.auth.dto.PasswordResetRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class PasswordResetIntegrationTest : IntegrationTestBase() {

    @BeforeEach
    fun registerUser() {
        authClient.registerWithLogin("testuser", "test@example.com", "oldpassword123")
    }

    @Test
    fun `request reset returns 200 for existing email`() {
        val response = authClient.requestPasswordReset(PasswordResetRequest("test@example.com"))

        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun `request reset returns 400 for non-existing email`() {
        val response = authClient.requestPasswordResetExpectError(PasswordResetRequest("nobody@example.com"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("auth.user_not_found", response.body?.code)
    }

    @Test
    fun `request reset stores code in Redis`() {
        authClient.requestPasswordReset(PasswordResetRequest("test@example.com"))

        val code = redisTemplate.opsForValue().get("password_reset:test@example.com")
        assertNotNull(code)
        assertEquals(6, code!!.length)
    }

    @Test
    fun `confirm reset changes password successfully`() {
        authClient.requestPasswordReset(PasswordResetRequest("test@example.com"))
        val code = redisTemplate.opsForValue().get("password_reset:test@example.com")!!

        val response = authClient.confirmPasswordReset(PasswordResetConfirm(code, "test@example.com", "newpassword456"))

        assertEquals(HttpStatus.OK, response.statusCode)

        val loginResponse = authClient.login(LoginRequest("testuser", "newpassword456"))
        assertEquals(HttpStatus.OK, loginResponse.statusCode)
    }

    @Test
    fun `old password does not work after reset`() {
        authClient.requestPasswordReset(PasswordResetRequest("test@example.com"))
        val code = redisTemplate.opsForValue().get("password_reset:test@example.com")!!

        authClient.confirmPasswordReset(PasswordResetConfirm(code, "test@example.com", "newpassword456"))

        val response = authClient.loginExpectError(LoginRequest("testuser", "oldpassword123"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("auth.invalid_credentials", response.body?.code)
    }

    @Test
    fun `confirm reset returns 400 for wrong code`() {
        authClient.requestPasswordReset(PasswordResetRequest("test@example.com"))

        val response = authClient.confirmPasswordResetExpectError(PasswordResetConfirm("000000", "test@example.com", "newpassword456"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("auth.invalid_reset_code", response.body?.code)
    }

    @Test
    fun `confirm reset returns 400 when no reset was requested`() {
        val response = authClient.confirmPasswordResetExpectError(PasswordResetConfirm("123456", "test@example.com", "newpassword456"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("auth.reset_code_expired", response.body?.code)
    }

    @Test
    fun `confirm reset returns 400 for non-existing user email`() {
        redisTemplate.opsForValue().set("password_reset:ghost@example.com", "123456")

        val response = authClient.confirmPasswordResetExpectError(PasswordResetConfirm("123456", "ghost@example.com", "newpassword456"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("auth.user_not_found", response.body?.code)
    }

    @Test
    fun `code is deleted from Redis after successful reset`() {
        authClient.requestPasswordReset(PasswordResetRequest("test@example.com"))
        val code = redisTemplate.opsForValue().get("password_reset:test@example.com")!!

        authClient.confirmPasswordReset(PasswordResetConfirm(code, "test@example.com", "newpassword456"))

        val storedCode = redisTemplate.opsForValue().get("password_reset:test@example.com")
        assertEquals(null, storedCode)
    }

    @Test
    fun `code cannot be reused after successful reset`() {
        authClient.requestPasswordReset(PasswordResetRequest("test@example.com"))
        val code = redisTemplate.opsForValue().get("password_reset:test@example.com")!!

        authClient.confirmPasswordReset(PasswordResetConfirm(code, "test@example.com", "newpassword456"))

        val response = authClient.confirmPasswordResetExpectError(PasswordResetConfirm(code, "test@example.com", "anotherpassword"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("auth.reset_code_expired", response.body?.code)
    }

    @Test
    fun `request reset returns 400 for invalid email format`() {
        val response = authClient.requestPasswordReset(mapOf("email" to "not-an-email"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("validation_error", response.body?.code)
    }

    @Test
    fun `confirm reset returns 400 when new password is too short`() {
        val response = authClient.confirmPasswordResetExpectError(PasswordResetConfirm("123456", "test@example.com", "12345"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("validation_error", response.body?.code)
    }
}
