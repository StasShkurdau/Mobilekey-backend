package com.mobilekey.backend.file

import com.mobilekey.backend.IntegrationTestBase
import com.mobilekey.backend.auth.dto.RegisterRequest
import com.mobilekey.backend.auth.dto.TokenResponse
import com.mobilekey.backend.user.dto.UpdateUserRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

class FileIntegrationTest : IntegrationTestBase() {

    private lateinit var tokens: TokenResponse

    @BeforeEach
    fun registerUser() {
        tokens = authClient.register(RegisterRequest("testuser", "test@example.com", "password123")).body!!
    }

    // --- Upload ---

    @Test
    fun `upload file returns 201 with file response`() {
        val response = authClient.uploadFile(tokens.accessToken)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body?.id)
        assertEquals("test.png", response.body?.name)
        assertEquals("image/png", response.body?.contentType)
        assertEquals("TEMP", response.body?.status)
    }

    @Test
    fun `upload file with jpeg content type succeeds`() {
        val response = authClient.uploadFile(
            tokens.accessToken,
            fileName = "photo.jpg",
            contentType = MediaType.IMAGE_JPEG,
        )

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("image/jpeg", response.body?.contentType)
    }

    @Test
    fun `upload file with disallowed content type returns 400`() {
        val response = authClient.uploadFileExpectError(
            tokens.accessToken,
            fileName = "doc.pdf",
            contentType = MediaType.APPLICATION_PDF,
        )

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("file.invalid_type", response.body?.code)
    }

    @Test
    fun `upload file exceeding max size returns 400`() {
        val largeContent = ByteArray(10_485_761) { 0x42 } // 10MB + 1 byte

        val response = authClient.uploadFileExpectError(
            tokens.accessToken,
            content = largeContent,
        )

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("file.too_large", response.body?.code)
    }

    @Test
    fun `upload without auth returns 401`() {
        val result = webTestClient.post()
            .uri("/api/files")
            .exchange()

        result.expectStatus().isUnauthorized
    }

    // --- Download ---

    @Test
    fun `download uploaded file returns 200 with correct content type`() {
        val uploadResponse = authClient.uploadFile(tokens.accessToken)
        val fileId = uploadResponse.body!!.id.toString()

        val result = authClient.downloadFile(tokens.accessToken, fileId)

        result.expectStatus().isOk
            .expectHeader().contentType(MediaType.IMAGE_PNG)
    }

    @Test
    fun `download non-existent file returns 404`() {
        val result = authClient.downloadFile(tokens.accessToken, "00000000-0000-0000-0000-000000000000")

        result.expectStatus().isNotFound
    }

    // --- Delete ---

    @Test
    fun `delete uploaded file returns 204`() {
        val uploadResponse = authClient.uploadFile(tokens.accessToken)
        val fileId = uploadResponse.body!!.id.toString()

        val result = authClient.deleteFile(tokens.accessToken, fileId)

        result.expectStatus().isNoContent
    }

    @Test
    fun `deleted file cannot be downloaded`() {
        val uploadResponse = authClient.uploadFile(tokens.accessToken)
        val fileId = uploadResponse.body!!.id.toString()

        authClient.deleteFile(tokens.accessToken, fileId)
        val result = authClient.downloadFile(tokens.accessToken, fileId)

        result.expectStatus().isNotFound
    }

    @Test
    fun `delete non-existent file returns 404`() {
        val result = authClient.deleteFile(tokens.accessToken, "00000000-0000-0000-0000-000000000000")

        result.expectStatus().isNotFound
    }

    // --- Avatar flow ---

    @Test
    fun `upload file and set as avatar returns avatar id in profile`() {
        val fileId = authClient.uploadFile(tokens.accessToken).body!!.id

        val updateResponse = authClient.updateProfile(
            tokens.accessToken,
            UpdateUserRequest(avatarId = fileId),
        )

        assertEquals(HttpStatus.OK, updateResponse.statusCode)
        assertEquals(fileId, updateResponse.body?.avatarId)
    }

    @Test
    fun `avatar id is returned in get profile`() {
        val fileId = authClient.uploadFile(tokens.accessToken).body!!.id

        authClient.updateProfile(tokens.accessToken, UpdateUserRequest(avatarId = fileId))

        val profile = authClient.getProfile(tokens.accessToken)
        assertEquals(fileId, profile.body?.avatarId)
    }

    @Test
    fun `avatar file is downloadable after being set`() {
        val fileId = authClient.uploadFile(tokens.accessToken).body!!.id

        authClient.updateProfile(tokens.accessToken, UpdateUserRequest(avatarId = fileId))

        authClient.downloadFile(tokens.accessToken, fileId.toString()).expectStatus().isOk
    }

    @Test
    fun `profile without avatar returns null avatar id`() {
        val profile = authClient.getProfile(tokens.accessToken)

        assertNull(profile.body?.avatarId)
    }

    @Test
    fun `updating avatar with same id does not change anything`() {
        val fileId = authClient.uploadFile(tokens.accessToken).body!!.id
        authClient.updateProfile(tokens.accessToken, UpdateUserRequest(avatarId = fileId))

        val updateResponse = authClient.updateProfile(
            tokens.accessToken,
            UpdateUserRequest(avatarId = fileId),
        )

        assertEquals(fileId, updateResponse.body?.avatarId)
        authClient.downloadFile(tokens.accessToken, fileId.toString()).expectStatus().isOk
    }

    @Test
    fun `replacing avatar moves old file to deleted and new file becomes downloadable`() {
        val firstFileId = authClient.uploadFile(tokens.accessToken).body!!.id
        val secondFileId = authClient.uploadFile(tokens.accessToken).body!!.id

        authClient.updateProfile(tokens.accessToken, UpdateUserRequest(avatarId = firstFileId))
        authClient.updateProfile(tokens.accessToken, UpdateUserRequest(avatarId = secondFileId))

        // new avatar is downloadable
        authClient.downloadFile(tokens.accessToken, secondFileId.toString()).expectStatus().isOk
        // old avatar moved to deleted — still findable in DB (until worker cleans it up)
        authClient.downloadFile(tokens.accessToken, firstFileId.toString()).expectStatus().isOk
    }

    @Test
    fun `removing avatar by passing null clears avatar id`() {
        val fileId = authClient.uploadFile(tokens.accessToken).body!!.id
        authClient.updateProfile(tokens.accessToken, UpdateUserRequest(avatarId = fileId))

        val updateResponse = authClient.updateProfile(
            tokens.accessToken,
            UpdateUserRequest(avatarId = null),
        )

        assertNull(updateResponse.body?.avatarId)
    }
}
