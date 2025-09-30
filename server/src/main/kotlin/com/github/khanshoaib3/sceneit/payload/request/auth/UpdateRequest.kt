package com.github.khanshoaib3.sceneit.payload.request.auth

import jakarta.validation.constraints.Email

data class UpdateRequest(
    val username: String? = null,

    @field:Email
    val email: String? = null,
)