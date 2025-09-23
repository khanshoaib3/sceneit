package com.github.khanshoaib3.sceneit

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class RegisterCredentials(
    val username: String,
    val password: String,
    val email: String? = null,
)

data class LoginCredentials(
    val username: String,
    val password: String,
)

data class ApiResponse(val status: Int, val message: String)

@RestController
@RequestMapping("/auth")
class AuthController(private val userRepository: UserRepository) {
    private val passwordEncoder = BCryptPasswordEncoder(16)

    @PostMapping("/register")
    fun register(@RequestBody credentials: RegisterCredentials): ResponseEntity<Any> {
        if (userRepository.existsByUsername(credentials.username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse(HttpStatus.CONFLICT.value(), "Username already exists."))
        }
        if (!credentials.email.isNullOrEmpty() && userRepository.existsByEmail(credentials.email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse(HttpStatus.CONFLICT.value(), "Email already exists."))
        }
        if (credentials.password.count() < 8) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(HttpStatus.CONFLICT.value(), "Password needs to be at-least 8 chars long."))
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(userRepository.save<UserEntity>(UserEntity().apply {
            username = credentials.username
            // https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html#authentication-password-storage-bcrypt
            password = passwordEncoder.encode(credentials.password)
        }))
    }

    @PostMapping("/login")
    fun login(@RequestBody credentials: LoginCredentials): ResponseEntity<ApiResponse> {
        val userEntity = userRepository.findByUsername(credentials.username)
        if (userEntity != null) {
            if (passwordEncoder.matches(credentials.password, userEntity.password))
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(ApiResponse(HttpStatus.ACCEPTED.value(), "TODO Send JWT token"))
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse(HttpStatus.BAD_REQUEST.value(), "Username or password does not matches."))
    }
    // info
    // update
    // delete
}