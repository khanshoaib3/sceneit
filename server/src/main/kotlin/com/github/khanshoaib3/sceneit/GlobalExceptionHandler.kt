package com.github.khanshoaib3.sceneit

import com.github.khanshoaib3.sceneit.controller.ApiResponse
import com.github.khanshoaib3.sceneit.controller.BusinessExceptions
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(BusinessExceptions::class)
    fun handleBusinessExceptions(ex: BusinessExceptions): ResponseEntity<ApiResponse> {
        val response = ApiResponse(
            status = ex.httpStatus.value(),
            message = ex.message
        )
        return ResponseEntity.status(ex.httpStatus).body(response)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleFieldValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse> {
        val errors = ex.bindingResult.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }

        val response = ApiResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = errors
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(ex: ConstraintViolationException): ResponseEntity<ApiResponse> {
        val errors = ex.constraintViolations.joinToString(";") {"${it.propertyPath}: ${it.message}"}

        val response = ApiResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = errors
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }
}