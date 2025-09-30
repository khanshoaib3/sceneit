package com.github.khanshoaib3.sceneit.controller

import com.github.khanshoaib3.sceneit.exception.BusinessExceptions
import com.github.khanshoaib3.sceneit.model.UserEntity
import com.github.khanshoaib3.sceneit.payload.request.auth.DeleteRequest
import com.github.khanshoaib3.sceneit.payload.request.auth.LoginRequest
import com.github.khanshoaib3.sceneit.payload.request.auth.PasswordUpdateRequest
import com.github.khanshoaib3.sceneit.payload.request.auth.RegisterRequest
import com.github.khanshoaib3.sceneit.payload.request.auth.UpdateRequest
import com.github.khanshoaib3.sceneit.payload.response.auth.LoginResponse
import com.github.khanshoaib3.sceneit.payload.response.MessageResponse
import com.github.khanshoaib3.sceneit.payload.response.auth.UserInfoResponse
import com.github.khanshoaib3.sceneit.payload.response.auth.toUserInfoResponse
import com.github.khanshoaib3.sceneit.repository.UserRepository
import com.github.khanshoaib3.sceneit.security.jwt.JwtUtils
import com.github.khanshoaib3.sceneit.security.services.UserDetailsImpl
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userRepository: UserRepository,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtUtils: JwtUtils,
) {
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<Any> {
        if (userRepository.existsByUsername(request.username)) {
            throw BusinessExceptions.UsernameExistException()
        }
        if (!request.email.isNullOrEmpty() && userRepository.existsByEmail(request.email)) {
            throw BusinessExceptions.EmailExistException()
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
            userRepository.save<UserEntity>(
                UserEntity(
                    username = request.username,
                    // https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html#authentication-password-storage-bcrypt
                    password = passwordEncoder.encode(request.password), email = request.email
                )
            )
        )
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        try {
            val authentication: Authentication = authenticationManager
                .authenticate(UsernamePasswordAuthenticationToken(request.username, request.password))

            SecurityContextHolder.getContext().authentication = authentication
            val jwt: String = jwtUtils.generateJwtToken(authentication)

            return ResponseEntity.ok(LoginResponse(token = jwt))
        } catch (ex: BadCredentialsException) {
            throw BusinessExceptions.PasswordIncorrectException()
        }
    }

    @GetMapping("/validate")
    fun validate(): ResponseEntity<Boolean> {
        return ResponseEntity.ok(true)
    }

    @GetMapping("/info")
    fun info(): ResponseEntity<UserInfoResponse> {
        val userDetails = SecurityContextHolder.getContext().authentication.principal as UserDetailsImpl
        return ResponseEntity.ok(
            userDetails.let { userRepository.findByUsername(it.username)?.toUserInfoResponse() }
                ?: throw BusinessExceptions.UserNotFoundException()
        )
    }

    @PutMapping("/update")
    @Transactional
    fun update(@Valid @RequestBody request: UpdateRequest): ResponseEntity<MessageResponse> {
        val userDetails = SecurityContextHolder.getContext().authentication.principal as UserDetailsImpl
        val userEntity =
            userRepository.findByUsername(userDetails.username) ?: throw BusinessExceptions.UsernameNotFoundException()

        if (!request.username.isNullOrBlank() && request.username != userEntity.username) {
            if (userRepository.existsByUsername(request.username)) throw BusinessExceptions.UsernameExistException()
            userEntity.username = request.username
        }

        if (!request.email.isNullOrBlank() && request.email != userEntity.email) {
            if (userRepository.existsByEmail(request.email)) throw BusinessExceptions.EmailExistException()
            userEntity.email = request.email
        }

        userRepository.save(userEntity)

        // TODO Return new jwt if username was changed
        return ResponseEntity.ok(MessageResponse("Updated successfully"))
    }

    @PutMapping("/password-update")
    @Transactional
    fun passwordUpdate(@Valid @RequestBody request: PasswordUpdateRequest): ResponseEntity<MessageResponse> {
        if (request.newPassword == request.oldPassword) throw BusinessExceptions.SamePasswordException()
        val userDetails = SecurityContextHolder.getContext().authentication.principal as UserDetailsImpl
        val userEntity =
            userRepository.findByUsername(userDetails.username) ?: throw BusinessExceptions.UsernameNotFoundException()

        if (!passwordEncoder.matches(request.oldPassword, userEntity.password)) {
            throw BusinessExceptions.PasswordIncorrectException()
        }

        userEntity.password = passwordEncoder.encode(request.newPassword)
        userRepository.save(userEntity)

        return ResponseEntity.ok(MessageResponse("Password updated successfully!"))
    }

    @DeleteMapping("/delete")
    fun delete(@Valid @RequestBody request: DeleteRequest): ResponseEntity<MessageResponse> {
        val userDetails = SecurityContextHolder.getContext().authentication.principal as UserDetailsImpl
        if (!passwordEncoder.matches(request.password, userDetails.password)) {
            throw BusinessExceptions.PasswordIncorrectException()
        }
        userRepository.deleteById(userDetails.getId()!!)
        return ResponseEntity.ok(MessageResponse("User delete successfully"))
    }
}