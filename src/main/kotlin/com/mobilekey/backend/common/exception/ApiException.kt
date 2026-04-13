package com.mobilekey.backend.common.exception

class ApiException(val error: ApiError) : RuntimeException(error.message)
