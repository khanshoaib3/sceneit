package com.github.khanshoaib3.sceneit.security.jwt

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint

/**
  * This method will be triggered anytime unauthenticated User requests a secured HTTP resource and an AuthenticationException is thrown.
 **/
class AuthEntryPointJwt : AuthenticationEntryPoint {
    private val logger: Logger = LoggerFactory.getLogger(AuthEntryPointJwt::class.java)

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        logger.error("Unauthorized error: {}", authException.message);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
    }
}