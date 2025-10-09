package com.github.khanshoaib3.sceneit.repository

import com.github.khanshoaib3.sceneit.model.MediaEntity
import com.github.khanshoaib3.sceneit.model.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface MediaRepository : JpaRepository<MediaEntity, Long> {
    fun findByUser(user: UserEntity): List<MediaEntity>?
    fun findByUserAndId(user: UserEntity, id: Long): MediaEntity?
}