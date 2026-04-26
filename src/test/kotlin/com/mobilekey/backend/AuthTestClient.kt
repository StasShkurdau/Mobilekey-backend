package com.mobilekey.backend

import com.mobilekey.backend.auth.dto.LoginRequest
import com.mobilekey.backend.auth.dto.PasswordResetConfirm
import com.mobilekey.backend.auth.dto.PasswordResetRequest
import com.mobilekey.backend.auth.dto.RefreshRequest
import com.mobilekey.backend.auth.dto.RegisterRequest
import com.mobilekey.backend.auth.dto.TokenResponse
import com.mobilekey.backend.common.dto.ErrorResponse
import com.mobilekey.backend.common.dto.MessageResponse
import com.mobilekey.backend.file.dto.FileResponse
import com.mobilekey.backend.user.dto.UpdateUserRequest
import com.mobilekey.backend.user.dto.UserResponse
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.web.reactive.server.WebTestClient

class AuthTestClient(private val webTestClient: WebTestClient) {

    // --- Register ---

    fun register(request: RegisterRequest): TestResponse<TokenResponse> =
        post("/api/auth/register", request)

    fun register(body: Map<String, Any?>): TestResponse<ErrorResponse> =
        post("/api/auth/register", body)

    fun registerExpectError(request: RegisterRequest): TestResponse<ErrorResponse> =
        post("/api/auth/register", request)

    // --- Login ---

    fun login(request: LoginRequest): TestResponse<TokenResponse> =
        post("/api/auth/login", request)

    fun login(body: Map<String, Any?>): TestResponse<ErrorResponse> =
        post("/api/auth/login", body)

    fun loginExpectError(request: LoginRequest): TestResponse<ErrorResponse> =
        post("/api/auth/login", request)

    // --- Refresh ---

    fun refresh(request: RefreshRequest): TestResponse<TokenResponse> =
        post("/api/auth/refresh", request)

    fun refresh(body: Map<String, Any?>): TestResponse<ErrorResponse> =
        post("/api/auth/refresh", body)

    fun refreshExpectError(request: RefreshRequest): TestResponse<ErrorResponse> =
        post("/api/auth/refresh", request)

    // --- Logout ---

    fun logout(accessToken: String): TestResponse<MessageResponse> =
        postWithAuth("/api/auth/logout", null, accessToken)

    // --- Password Reset ---

    fun requestPasswordReset(request: PasswordResetRequest): TestResponse<MessageResponse> =
        post("/api/auth/password-reset/request", request)

    fun requestPasswordReset(body: Map<String, Any?>): TestResponse<ErrorResponse> =
        post("/api/auth/password-reset/request", body)

    fun requestPasswordResetExpectError(request: PasswordResetRequest): TestResponse<ErrorResponse> =
        post("/api/auth/password-reset/request", request)

    fun confirmPasswordReset(request: PasswordResetConfirm): TestResponse<MessageResponse> =
        post("/api/auth/password-reset/confirm", request)

    fun confirmPasswordResetExpectError(request: PasswordResetConfirm): TestResponse<ErrorResponse> =
        post("/api/auth/password-reset/confirm", request)

    // --- Profile ---

    fun getProfile(accessToken: String): TestResponse<UserResponse> {
        val result = webTestClient.get()
            .uri("/api/users/me")
            .headers { it.setBearerAuth(accessToken) }
            .exchange()
            .returnResult(UserResponse::class.java)
        val status = result.status
        val body = result.responseBody.blockFirst()
        return TestResponse(status, body)
    }

    fun updateProfile(accessToken: String, request: UpdateUserRequest): TestResponse<UserResponse> =
        putWithAuth("/api/users/me", request, accessToken)

    fun updateProfileExpectError(accessToken: String, request: UpdateUserRequest): TestResponse<ErrorResponse> =
        putWithAuth("/api/users/me", request, accessToken)

    // --- Files ---

    fun uploadFile(
        accessToken: String,
        fileName: String = "test.png",
        contentType: MediaType = MediaType.IMAGE_PNG,
        content: ByteArray = ByteArray(100) { 0x42 },
    ): TestResponse<FileResponse> {
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("file", object : ByteArrayResource(content) {
            override fun getFilename() = fileName
        }).contentType(contentType)

        val result = webTestClient.post()
            .uri("/api/files")
            .headers { it.setBearerAuth(accessToken) }
            .bodyValue(bodyBuilder.build())
            .exchange()
            .returnResult(FileResponse::class.java)
        val status = result.status
        val body = result.responseBody.blockFirst()
        return TestResponse(status, body)
    }

    fun uploadFileExpectError(
        accessToken: String,
        fileName: String = "test.png",
        contentType: MediaType = MediaType.IMAGE_PNG,
        content: ByteArray = ByteArray(100) { 0x42 },
    ): TestResponse<ErrorResponse> {
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("file", object : ByteArrayResource(content) {
            override fun getFilename() = fileName
        }).contentType(contentType)

        val result = webTestClient.post()
            .uri("/api/files")
            .headers { it.setBearerAuth(accessToken) }
            .bodyValue(bodyBuilder.build())
            .exchange()
            .returnResult(ErrorResponse::class.java)
        val status = result.status
        val body = result.responseBody.blockFirst()
        return TestResponse(status, body)
    }

    fun downloadFile(accessToken: String, fileId: String): WebTestClient.ResponseSpec {
        return webTestClient.get()
            .uri("/api/files/$fileId")
            .headers { it.setBearerAuth(accessToken) }
            .exchange()
    }

    fun deleteFile(accessToken: String, fileId: String): WebTestClient.ResponseSpec {
        return webTestClient.delete()
            .uri("/api/files/$fileId")
            .headers { it.setBearerAuth(accessToken) }
            .exchange()
    }

    // --- Helpers ---

    private inline fun <reified T : Any> post(uri: String, body: Any?): TestResponse<T> {
        val result = webTestClient.post()
            .uri(uri)
            .let { if (body != null) it.bodyValue(body) else it }
            .exchange()
            .returnResult(T::class.java)
        val status = result.status
        val responseBody = result.responseBody.blockFirst()
        return TestResponse(status, responseBody)
    }

    private inline fun <reified T : Any> postWithAuth(uri: String, body: Any?, accessToken: String): TestResponse<T> {
        val result = webTestClient.post()
            .uri(uri)
            .headers { it.setBearerAuth(accessToken) }
            .let { if (body != null) it.bodyValue(body) else it }
            .exchange()
            .returnResult(T::class.java)
        val status = result.status
        val responseBody = result.responseBody.blockFirst()
        return TestResponse(status, responseBody)
    }

    private inline fun <reified T : Any> putWithAuth(uri: String, body: Any?, accessToken: String): TestResponse<T> {
        val result = webTestClient.put()
            .uri(uri)
            .headers { it.setBearerAuth(accessToken) }
            .let { if (body != null) it.bodyValue(body) else it }
            .exchange()
            .returnResult(T::class.java)
        val status = result.status
        val responseBody = result.responseBody.blockFirst()
        return TestResponse(status, responseBody)
    }
}

data class TestResponse<T>(
    val statusCode: HttpStatusCode,
    val body: T?,
)
