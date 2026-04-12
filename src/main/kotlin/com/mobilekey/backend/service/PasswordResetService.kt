package com.mobilekey.backend.service

import com.mobilekey.backend.config.PasswordResetProperties
import com.mobilekey.backend.repository.UserRepository
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
        val user = userRepository.findByEmail(email) ?: return

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
            ?: throw IllegalArgumentException("Reset code expired or not found")

        if (storedCode != code) {
            throw IllegalArgumentException("Invalid reset code")
        }

        val user = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("User not found")

        val updated = user.copy(password = passwordEncoder.encode(newPassword))
        userRepository.update(updated)

        redisTemplate.delete(PREFIX + email)
    }

    private fun generateCode(): String {
        return String.format("%06d", Random.nextInt(1_000_000))
    }
}
