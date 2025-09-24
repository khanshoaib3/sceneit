package com.github.khanshoaib3.sceneit

import jakarta.validation.Valid
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

sealed class BusinessExceptions(val httpStatus: HttpStatus, override val message: String) : Exception(message) {
    class UsernameExistException() : BusinessExceptions(HttpStatus.CONFLICT, "Username already exists.")
    class UsernameNotFoundException() : BusinessExceptions(HttpStatus.BAD_REQUEST, "Username not found.")
    class EmailExistException() : BusinessExceptions(HttpStatus.CONFLICT, "Email already exists.")
    class PasswordIncorrectException() : BusinessExceptions(HttpStatus.BAD_REQUEST, "Password is incorrect.")
}

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userRepository: UserRepository,
    private val passwordEncoder: BCryptPasswordEncoder,
) {
    @PostMapping("/register")
    fun register(@Valid @RequestBody credentials: UserEntity): ResponseEntity<Any> {
        if (userRepository.existsByUsername(credentials.username)) {
            throw BusinessExceptions.UsernameExistException()
        }
        if (!credentials.email.isNullOrEmpty() && userRepository.existsByEmail(credentials.email!!)) {
            throw BusinessExceptions.EmailExistException()
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
            userRepository.save<UserEntity>(
                UserEntity(
                    username = credentials.username,
                    // https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html#authentication-password-storage-bcrypt
                    password = passwordEncoder.encode(credentials.password), email = credentials.email
                )
            )
        )
    }

    @PostMapping("/login")
    fun login(@RequestBody credentials: LoginCredentials): ResponseEntity<ApiResponse> {
        val userEntity = userRepository.findByUsername(credentials.username)
            ?: throw BusinessExceptions.UsernameNotFoundException()
        if (passwordEncoder.matches(
                credentials.password,
                userEntity.password
            )
        ) return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(ApiResponse(HttpStatus.ACCEPTED.value(), "TODO Send JWT token"))

        throw BusinessExceptions.PasswordIncorrectException()
    }
    // info
    // update
    // delete
}