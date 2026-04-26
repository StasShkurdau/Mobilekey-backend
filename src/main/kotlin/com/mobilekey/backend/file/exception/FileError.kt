package com.mobilekey.backend.file.exception

import com.mobilekey.backend.common.exception.ApiError
import org.springframework.http.HttpStatus

enum class FileError(
    override val code: String,
    override val message: String,
    override val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST,
) : ApiError {
    FILE_NOT_FOUND("file.not_found", "File not found", HttpStatus.NOT_FOUND),
    FILE_TOO_LARGE("file.too_large", "File exceeds maximum size"),
    INVALID_FILE_TYPE("file.invalid_type", "File type not allowed"),
    FILE_UPLOAD_FAILED("file.upload_failed", "Failed to upload file", HttpStatus.INTERNAL_SERVER_ERROR),
}
