package com.github.khanshoaib3.sceneit

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun providePasswordEncoder() = BCryptPasswordEncoder(16)

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() } // Not needed as this is a REST API
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/auth/register", "/auth/login").permitAll()
                    .anyRequest().authenticated()
            }
            .build()
    }
}