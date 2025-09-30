package com.github.khanshoaib3.sceneit.security.services

import com.github.khanshoaib3.sceneit.exception.BusinessExceptions
import com.github.khanshoaib3.sceneit.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService

class UserDetailServiceImpl(private val userRepository: UserRepository) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails =
        userRepository.findByUsername(username)?.toUserDetail() ?: throw BusinessExceptions.UsernameNotFoundException()
}