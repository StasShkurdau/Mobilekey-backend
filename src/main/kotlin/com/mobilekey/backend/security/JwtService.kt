package com.mobilekey.backend.security

import com.mobilekey.backend.config.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Service
class JwtService(private val jwtProperties: JwtProperties) {

    private val accessKey: SecretKey = Keys.hmacShaKeyFor(jwtProperties.accessSecret.toByteArray())
    private val refreshKey: SecretKey = Keys.hmacShaKeyFor(jwtProperties.refreshSecret.toByteArray())

    fun generateAccessToken(userId: UUID): String {
        return generateToken(userId, jwtProperties.accessExpirationMs, accessKey)
    }

    fun generateRefreshToken(userId: UUID): String {
        return generateToken(userId, jwtProperties.refreshExpirationMs, refreshKey)
    }

    fun validateAccessToken(token: String): UUID? {
        return extractUserId(token, accessKey)
    }

    fun validateRefreshToken(token: String): UUID? {
        return extractUserId(token, refreshKey)
    }

    private fun generateToken(userId: UUID, expirationMs: Long, key: SecretKey): String {
        val now = Date()
        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiration(Date(now.time + expirationMs))
            .signWith(key)
            .compact()
    }

    private fun extractUserId(token: String, key: SecretKey): UUID? {
        return try {
            val claims: Claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
            UUID.fromString(claims.subject)
        } catch (e: Exception) {
            null
        }
    }
}
