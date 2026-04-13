package com.mobilekey.backend.auth.service

import com.mobilekey.backend.auth.config.PasswordResetProperties
import com.mobilekey.backend.auth.exception.AuthError
import com.mobilekey.backend.common.exception.ApiException
import com.mobilekey.backend.user.repository.UserRepository
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@Service
class PasswordResetService(
    private val userRepository: UserRepository,
    private val redisTemplate: StringRedisTemplate,
    private val mailSender: JavaMailSender,
    private val passwordEncoder: PasswordEncoder,
    private val passwordResetProperties: PasswordResetProperties,
) {

    companion object {
        private const val PREFIX = "password_reset:"
    }

    fun requestReset(email: String) {
        val user = userRepository.findByEmail(email)
            ?: throw ApiException(AuthError.USER_NOT_FOUND)

        val code = generateCode()
        redisTemplate.opsForValue().set(
            PREFIX + email,
            code,
            passwordResetProperties.expirationMs,
            TimeUnit.MILLISECONDS,
        )

        val message = SimpleMailMessage().apply {
            setTo(email)
            subject = "Password Reset Code"
            text = "Your password reset code: $code\nThis code expires in 15 minutes."
        }

        mailSender.send(message)
    }

    fun confirmReset(email: String, code: String, newPassword: String) {
        val storedCode = redisTemplate.opsForValue().get(PREFIX + email)
            ?: throw ApiException(AuthError.RESET_CODE_EXPIRED)

        if (storedCode != code) {
            throw ApiException(AuthError.INVALID_RESET_CODE)
        }

        val user = userRepository.findByEmail(email)
            ?: throw ApiException(AuthError.USER_NOT_FOUND)

        val updated = user.copy(password = passwordEncoder.encode(newPassword))

        userRepository.update(updated)

        redisTemplate.delete(PREFIX + email)
    }

    private fun generateCode(): String {
        return String.format("%06d", Random.nextInt(1_000_000))
    }
}
