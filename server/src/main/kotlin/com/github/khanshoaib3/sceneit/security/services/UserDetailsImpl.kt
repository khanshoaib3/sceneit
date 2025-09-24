package com.github.khanshoaib3.sceneit.security.services

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.khanshoaib3.sceneit.model.AuthRole
import com.github.khanshoaib3.sceneit.model.UserEntity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserDetailsImpl(
    private val id: Long?,
    private val username: String,
    @field:JsonIgnore private val password: String,
    private val email: String?,
    private val role: AuthRole,
    private var authorities: Collection<GrantedAuthority?>,
) : UserDetails {
    companion object {
        const val serialVersionUID: Long = 1L
    }

    fun getId() = id

    override fun getUsername() = username

    override fun getPassword() = password

    override fun getAuthorities(): Collection<GrantedAuthority?> = authorities

    fun getEmail() = email

    fun getRole() = role

    override fun equals(other: Any?): Boolean {
        if (this === other) return true // Check for reference (it's triple equal)
        if (other == null || other !is UserDetailsImpl) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        var result = serialVersionUID.hashCode()
        result = 31 * result + (id?.hashCode() ?: 0)
        return result
    }
}

fun UserEntity.toUserDetail(): UserDetailsImpl {
    val authorities: List<GrantedAuthority> = listOf(SimpleGrantedAuthority(this.role.name))

    return UserDetailsImpl(
        id = this.id,
        username = this.username,
        password = this.password,
        email = this.email,
        role = this.role,
        authorities = authorities,
    )
}
