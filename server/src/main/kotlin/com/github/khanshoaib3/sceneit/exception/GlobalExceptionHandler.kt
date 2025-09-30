package com.github.khanshoaib3.sceneit.exception

import com.github.khanshoaib3.sceneit.payload.response.MessageResponse
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(BusinessExceptions::class)
    fun handleBusinessExceptions(ex: BusinessExceptions): ResponseEntity<MessageResponse> {
        val response = MessageResponse(
            message = ex.message
        )
        return ResponseEntity.status(ex.httpStatus).body(response)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleFieldValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<MessageResponse> {
        val errors = ex.bindingResult.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }

        val response = MessageResponse(
            message = errors
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(ex: ConstraintViolationException): ResponseEntity<MessageResponse> {
        val errors = ex.constraintViolations.joinToString(";") { "${it.propertyPath}: ${it.message}" }

        val response = MessageResponse(
            message = errors
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }
}