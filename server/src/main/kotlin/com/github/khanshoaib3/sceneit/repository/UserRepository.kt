package com.github.khanshoaib3.sceneit.repository

import com.github.khanshoaib3.sceneit.model.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

public interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByUsername(username: String): UserEntity?
    fun existsByUsername(username: String): Boolean

    fun existsByEmail(email: String): Boolean
}