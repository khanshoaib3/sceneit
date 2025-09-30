package com.github.khanshoaib3.sceneit.payload.request.auth

import jakarta.validation.constraints.Size

data class PasswordUpdateRequest(
    val oldPassword: String,

    @field:Size(min = 6, max = 16)
    val newPassword: String,
)