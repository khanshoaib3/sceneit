package com.github.khanshoaib3.sceneit.exception

import org.springframework.http.HttpStatus

sealed class BusinessExceptions(val httpStatus: HttpStatus, override val message: String) : Exception(message) {
    class UserNotFoundException() : BusinessExceptions(HttpStatus.UNAUTHORIZED, "User not found.")
    class UsernameExistException() : BusinessExceptions(HttpStatus.CONFLICT, "Username already exists.")
    class UsernameNotFoundException() : BusinessExceptions(HttpStatus.UNAUTHORIZED, "Username not found.")
    class EmailExistException() : BusinessExceptions(HttpStatus.CONFLICT, "Email already exists.")
    class PasswordIncorrectException() : BusinessExceptions(HttpStatus.UNAUTHORIZED, "Password is incorrect.")
    class SamePasswordException(): BusinessExceptions(HttpStatus.BAD_REQUEST, message = "New password cannot be the old password.")

    class InvalidTimestamp(): BusinessExceptions(HttpStatus.BAD_REQUEST, message = "Invalid timestamp provided")
    class InvalidMediaId(): BusinessExceptions(HttpStatus.BAD_REQUEST, message = "Invalid media id provided")
}