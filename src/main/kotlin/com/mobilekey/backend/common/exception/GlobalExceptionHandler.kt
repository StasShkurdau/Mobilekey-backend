package com.mobilekey.backend.common.exception

import com.mobilekey.backend.common.dto.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ApiException::class)
    fun handleApiException(e: ApiException): ResponseEntity<ErrorResponse> {
        val error = e.error
        return ResponseEntity.status(error.httpStatus).body(
            ErrorResponse(code = error.code, message = error.message),
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = e.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(code = "validation_error", message = message),
        )
    }
}
