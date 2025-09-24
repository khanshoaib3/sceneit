package com.github.khanshoaib3.sceneit

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserDetailsImpl(
    private var id: Long?,
    private var username: String,
    @field:JsonIgnore private var password: String,
    private var authorities: Collection<GrantedAuthority?>,
) : UserDetails {
    companion object {
        const val serialVersionUID: Long = 1L
    }

    override fun getAuthorities(): Collection<GrantedAuthority?> = authorities

    override fun getPassword(): String = password

    override fun getUsername(): String = username

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
        authorities = authorities
    )
}
