package com.mobilekey.backend.auth.service

import com.mobilekey.backend.auth.exception.AuthError
import com.mobilekey.backend.auth.repository.PasswordResetRepository
import com.mobilekey.backend.common.exception.ApiException
import com.mobilekey.backend.user.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class PasswordResetService(
    private val userRepository: UserRepository,
    private val passwordResetRepository: PasswordResetRepository,
    private val mailSender: JavaMailSender,
    private val passwordEncoder: PasswordEncoder,
) {

    suspend fun requestReset(email: String) {
        userRepository.findByEmail(email)
            ?: throw ApiException(AuthError.USER_NOT_FOUND)

        val code = generateCode()

        passwordResetRepository.save(email, code)

        val message = SimpleMailMessage().apply {
            setTo(email)
            subject = "Password Reset Code"
            text = "Your password reset code: $code\nThis code expires in 15 minutes."
        }

        withContext(Dispatchers.IO) {
            mailSender.send(message)
        }
    }

    suspend fun confirmReset(email: String, code: String, newPassword: String) {
        val storedCode = passwordResetRepository.find(email)
            ?: throw ApiException(AuthError.RESET_CODE_EXPIRED)

        if (storedCode != code) {
            throw ApiException(AuthError.INVALID_RESET_CODE)
        }

        val user = userRepository.findByEmail(email)
            ?: throw ApiException(AuthError.USER_NOT_FOUND)

        val updated = user.copy(password = passwordEncoder.encode(newPassword))

        userRepository.update(updated)

        passwordResetRepository.delete(email)
    }

    private fun generateCode(): String {
        return String.format("%06d", Random.nextInt(1_000_000))
    }
}
