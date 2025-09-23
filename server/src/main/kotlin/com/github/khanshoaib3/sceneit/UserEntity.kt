package com.github.khanshoaib3.sceneit

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.io.Serializable

@Entity
class UserEntity : Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(unique = true, nullable = false)
    var username: String? = null

    @Column(nullable = false)
    var password: String? = null

    @Column(unique = true, nullable = true)
    var email: String? = null
}

