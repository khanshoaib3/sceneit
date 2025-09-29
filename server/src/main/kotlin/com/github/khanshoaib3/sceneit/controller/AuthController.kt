package com.github.khanshoaib3.sceneit.controller

import com.github.khanshoaib3.sceneit.model.UserEntity
import com.github.khanshoaib3.sceneit.repository.UserRepository
import com.github.khanshoaib3.sceneit.security.jwt.JwtUtils
import com.github.khanshoaib3.sceneit.security.services.UserDetailsImpl
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*


data class RegisterRequest(
    val username: String,
    @field:Size(min = 6, max = 16)
    val password: String,
    val email: String? = null,
)

data class LoginRequest(
    val username: String,
    val password: String,
)

data class UpdateRequest(
    val username: String? = null,
    val email: String? = null,
)

data class PasswordUpdateRequest(
    val oldPassword: String,
    @field:Size(min = 6, max = 16)
    val newPassword: String,
)

data class DeleteRequest(
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
    class UserNotFoundException() : BusinessExceptions(HttpStatus.UNAUTHORIZED, "User not found.")
    class UsernameExistException() : BusinessExceptions(HttpStatus.CONFLICT, "Username already exists.")
    class UsernameNotFoundException() : BusinessExceptions(HttpStatus.UNAUTHORIZED, "Username not found.")
    class EmailExistException() : BusinessExceptions(HttpStatus.CONFLICT, "Email already exists.")
    class PasswordIncorrectException() : BusinessExceptions(HttpStatus.UNAUTHORIZED, "Password is incorrect.")
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
    fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<JwtResponse> {
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

    @GetMapping("/info")
    fun info(): ResponseEntity<Any> {
        val userDetails = SecurityContextHolder.getContext().authentication.principal as UserDetailsImpl
        return ResponseEntity.ok(
            userDetails.let { userRepository.findByUsername(it.username) }
                ?: throw BusinessExceptions.UserNotFoundException()
        )
    }

    @PutMapping("/update")
    @Transactional
    fun update(@Valid @RequestBody request: UpdateRequest): ResponseEntity<ApiResponse> {
        val userDetails = SecurityContextHolder.getContext().authentication.principal as UserDetailsImpl
        val userEntity = userRepository.findByUsername(userDetails.username) ?: throw BusinessExceptions.UsernameNotFoundException()

        if (!request.username.isNullOrBlank() && request.username != userEntity.username) {
            if(userRepository.existsByUsername(request.username)) throw BusinessExceptions.UsernameExistException()
            userEntity.username = request.username
        }

        if (!request.email.isNullOrBlank() && request.email != userEntity.email) {
            if(userRepository.existsByEmail(request.email)) throw BusinessExceptions.EmailExistException()
            userEntity.email = request.email
        }

        userRepository.save(userEntity)

        // TODO Return new jwt if username was changed
        return ResponseEntity.ok(ApiResponse(HttpStatus.OK.value(), "Updated successfully"))
    }

    @PutMapping("/password-update")
    @Transactional
    fun passwordUpdate(@Valid @RequestBody request: PasswordUpdateRequest): ResponseEntity<ApiResponse> {
        val userDetails = SecurityContextHolder.getContext().authentication.principal as UserDetailsImpl
        val userEntity = userRepository.findByUsername(userDetails.username) ?: throw BusinessExceptions.UsernameNotFoundException()

        if (!passwordEncoder.matches(request.oldPassword, userEntity.password)) {
            throw BusinessExceptions.PasswordIncorrectException()
        }

        userEntity.password = passwordEncoder.encode(request.newPassword)
        userRepository.save(userEntity)

        return ResponseEntity.ok(ApiResponse(HttpStatus.OK.value(), "Password updated successfully!"))
    }

    @DeleteMapping("/delete")
    fun delete(@Valid @RequestBody request: DeleteRequest): ResponseEntity<ApiResponse> {
        val userDetails = SecurityContextHolder.getContext().authentication.principal as UserDetailsImpl
        if (!passwordEncoder.matches(request.password, userDetails.password)) {
            throw BusinessExceptions.PasswordIncorrectException()
        }
        userRepository.deleteById(userDetails.getId()!!)
        return ResponseEntity.ok(ApiResponse(status = HttpStatus.OK.value(), "User delete successfully"))
    }
}