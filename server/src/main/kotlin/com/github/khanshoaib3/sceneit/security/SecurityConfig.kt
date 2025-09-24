package com.github.khanshoaib3.sceneit.security

import com.github.khanshoaib3.sceneit.repository.UserRepository
import com.github.khanshoaib3.sceneit.security.jwt.AuthEntryPointJwt
import com.github.khanshoaib3.sceneit.security.jwt.AuthTokenFilter
import com.github.khanshoaib3.sceneit.security.jwt.JwtUtils
import com.github.khanshoaib3.sceneit.security.services.UserDetailServiceImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val userRepository: UserRepository,
) {
    @Bean
    fun providePasswordEncoder() = BCryptPasswordEncoder(16)

    @Bean
    fun provideUserDetailsService() = UserDetailServiceImpl(userRepository)

    @Bean
    fun provideUnauthorizedHandler() = AuthEntryPointJwt()

    @Bean
    fun provideJwtUtils() = JwtUtils()

    @Bean
    fun provideAuthenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager =
        authenticationConfiguration.authenticationManager

    @Bean
    fun authenticationJwtTokenFilter() =
        AuthTokenFilter(jwtUtils = provideJwtUtils(), userDetailService = provideUserDetailsService())

    @Bean
    fun authenticationProvider() = DaoAuthenticationProvider(provideUserDetailsService()).apply {
        setPasswordEncoder(providePasswordEncoder())
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() } // Not needed as this is a REST API
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/auth/register", "/auth/login").permitAll()
                    .anyRequest().authenticated()
            }
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}