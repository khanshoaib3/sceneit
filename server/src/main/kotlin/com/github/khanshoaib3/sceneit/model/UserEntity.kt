package com.github.khanshoaib3.sceneit.model

import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.Instant

/*
sealed interface AuthRole {
    object USER : AuthRole
    object ADMIN : AuthRole
}
*/

enum class AuthRole { USER, ADMIN }

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["username"]),
        UniqueConstraint(columnNames = ["email"]),
    ]
)
// If we use a data class, we'll get copy() which can lead to bugs as copying will also copy the id
class UserEntity : Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @NotBlank
    @Size(max = 16)
    lateinit var username: String

    @NotBlank
    lateinit var password: String

    @Email(message = "Enter a valid email address")
    var email: String? = null

    @Enumerated(EnumType.STRING)
    var role: AuthRole = AuthRole.USER

    @CreatedDate
    @Column(name = "created_at")
    var createdAt: Instant? = null

    @LastModifiedDate
    @Column(name = "last_modified_at")
    var lastModifiedAt: Instant? = null

    constructor(username: String, password: String, email: String? = null) {
        this.username = username
        this.password = password
        this.email = email
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true // Compare reference
        if (id != null || other !is UserEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
