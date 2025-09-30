package com.github.khanshoaib3.sceneit.payload.response.auth

import com.github.khanshoaib3.sceneit.model.AuthRole
import com.github.khanshoaib3.sceneit.model.UserEntity
import jakarta.persistence.Column
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.time.Instant

data class UserInfoResponse(
    val username: String,
    val email: String?,
    @Enumerated(EnumType.STRING)
    val role: AuthRole,
    @Column(name = "created_at") val createdAt: Instant?,
    @Column(name = "last_modified_at") val lastModifiedAt: Instant?,
)

fun UserEntity.toUserInfoResponse() =
    UserInfoResponse(
        username = this.username,
        email = this.email,
        role = this.role,
        createdAt = this.createdAt,
        lastModifiedAt = this.lastModifiedAt
    )
