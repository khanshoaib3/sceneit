package com.github.khanshoaib3.sceneit.repository

import com.github.khanshoaib3.sceneit.model.MediaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface MediaRepository : JpaRepository<MediaEntity, Long> {
}