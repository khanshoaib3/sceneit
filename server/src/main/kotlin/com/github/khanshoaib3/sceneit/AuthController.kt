package com.github.khanshoaib3.sceneit

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class RegisterCredentials(
    val username: String,
    val password: String,
)

@RestController
@RequestMapping("/auth")
class AuthController {
    @PostMapping("/register")
    fun register(@RequestBody credentials : RegisterCredentials) : RegisterCredentials {
        return credentials
    }
}