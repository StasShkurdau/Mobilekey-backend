package com.mobilekey.backend.common.exception

import org.springframework.http.HttpStatus

interface ApiError {
    val code: String
    val message: String
    val httpStatus: HttpStatus
}
