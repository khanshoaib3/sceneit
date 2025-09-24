package com.github.khanshoaib3.sceneit.controller

import com.github.khanshoaib3.sceneit.security.services.UserDetailsImpl
import com.github.khanshoaib3.sceneit.model.UserEntity
import com.github.khanshoaib3.sceneit.repository.UserRepository
import com.github.khanshoaib3.sceneit.security.jwt.JwtUtils
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
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

data class JwtResponse(
    val jwt: String,
    val username: String,
    val email: String?,
    val role: String,
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
    private val authenticationManager: AuthenticationManager,
    private val jwtUtils: JwtUtils,
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
    fun login(@Valid @RequestBody loginRequest: LoginCredentials): ResponseEntity<JwtResponse> {
        try {
            val authentication: Authentication = authenticationManager
                .authenticate(UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password))

            SecurityContextHolder.getContext().authentication = authentication
            val jwt: String = jwtUtils.generateJwtToken(authentication)

            val userDetails = authentication.principal as UserDetailsImpl

            return ResponseEntity.ok(
                JwtResponse(
                    jwt = jwt,
                    username = userDetails.getUsername(),
                    email = userDetails.getEmail(),
                    role = userDetails.getRole().name
                )
            )
        } catch (ex: BadCredentialsException) {
            throw BusinessExceptions.PasswordIncorrectException()
        }
    }

    // info
    // update
    // delete
}