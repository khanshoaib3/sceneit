package com.github.khanshoaib3.sceneit.payload.request.auth

import jakarta.validation.constraints.Size

data class RegisterRequest(
    val username: String,

    @field:Size(min = 6, max = 16)
    val password: String,

    val email: String? = null,
)