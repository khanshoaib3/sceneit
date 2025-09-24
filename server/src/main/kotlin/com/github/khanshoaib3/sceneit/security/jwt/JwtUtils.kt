package com.github.khanshoaib3.sceneit.security.jwt

import com.github.khanshoaib3.sceneit.security.services.UserDetailsImpl
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import java.util.*
import javax.crypto.SecretKey
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException


// TODO Move this to application.properties
private const val jwtExpirationMs: Long =259200000

// Ref: https://github.com/jwtk/jjwt?tab=readme-ov-file#quickstart
class JwtUtils {
    @Value("\${sceneit.jwtSecret}")
    private var jwtSecret: String? = null


    private val key: SecretKey
        get() {
            if (jwtSecret == null) throw IllegalArgumentException("JwtSecret is null!")
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret))
        }

    private val logger: Logger = LoggerFactory.getLogger(JwtUtils::class.java)

    fun generateJwtToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal as UserDetailsImpl

        return Jwts.builder()
            .subject(userPrincipal.username)
            .issuedAt(Date())
            .expiration(Date(Date().time + jwtExpirationMs))
            .signWith(key)
            .compact()
    }

    fun getUsernameFromJwtToken(token: String): String {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload.subject
    }

    fun validateJwtToken(token: String): Boolean {
        try {
            Jwts.parser().verifyWith(key).build().parse(token)
            return true
        } catch (e: MalformedJwtException) {
            logger.error("Invalid JWT token: {}", e.message)
        } catch (e: ExpiredJwtException) {
            logger.error("JWT token is expired: {}", e.message)
        } catch (e: UnsupportedJwtException) {
            logger.error("JWT token is unsupported: {}", e.message)
        } catch (e: IllegalArgumentException) {
            logger.error("JWT claims string is empty: {}", e.message)
        } catch (e: JwtException) {
            logger.error("JWT token invalid: {}", e.message)
        }

        return false
    }
}