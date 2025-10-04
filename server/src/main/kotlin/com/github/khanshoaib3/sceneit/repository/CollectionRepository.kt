package com.github.khanshoaib3.sceneit.repository

import com.github.khanshoaib3.sceneit.model.CollectionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface CollectionRepository : JpaRepository<CollectionEntity, Long> {
}