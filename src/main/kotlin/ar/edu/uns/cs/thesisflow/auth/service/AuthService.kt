package ar.edu.uns.cs.thesisflow.auth.service

import ar.edu.uns.cs.thesisflow.auth.model.AuthUserPrincipal
import ar.edu.uns.cs.thesisflow.auth.repository.AuthUserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val authUserRepository: AuthUserRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    fun authenticate(username: String, password: String): Authentication {
        val authentication = UsernamePasswordAuthenticationToken(username, password)
        return authenticationManager.authenticate(authentication)
    }

    fun generateToken(authentication: Authentication): JwtToken {
        val principal = authentication.principal as? AuthUserPrincipal
            ?: throw IllegalStateException("Unexpected principal type: ${authentication.principal::class.simpleName}")
        return jwtService.generateToken(principal)
    }

    fun resetPassword(userId: Long, currentPassword: String, newPassword: String) {
        val user = authUserRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        if (!passwordEncoder.matches(currentPassword, user.password)) {
            throw IllegalArgumentException("Current password is incorrect")
        }

        if (newPassword.isBlank()) {
            throw IllegalArgumentException("New password cannot be empty")
        }

        if (newPassword.length < 8) {
            throw IllegalArgumentException("New password must be at least 8 characters long")
        }

        user.password = passwordEncoder.encode(newPassword)
        authUserRepository.save(user)
    }
}
