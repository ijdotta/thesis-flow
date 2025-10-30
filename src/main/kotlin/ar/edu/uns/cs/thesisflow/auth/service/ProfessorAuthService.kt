package ar.edu.uns.cs.thesisflow.auth.service

import ar.edu.uns.cs.thesisflow.auth.dto.RequestProfessorLoginLinkResponse
import ar.edu.uns.cs.thesisflow.auth.dto.VerifyProfessorLoginLinkResponse
import ar.edu.uns.cs.thesisflow.auth.model.AuthUser
import ar.edu.uns.cs.thesisflow.auth.model.AuthUserPrincipal
import ar.edu.uns.cs.thesisflow.auth.model.UserRole
import ar.edu.uns.cs.thesisflow.auth.persistance.entity.ProfessorLoginToken
import ar.edu.uns.cs.thesisflow.auth.persistance.repository.ProfessorLoginTokenRepository
import ar.edu.uns.cs.thesisflow.auth.repository.AuthUserRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.ProfessorRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

data class RateLimitEntry(
    val count: Int = 0,
    val resetTime: Long = 0,
)

@Service
class ProfessorAuthService(
    private val professorRepository: ProfessorRepository,
    private val professorLoginTokenRepository: ProfessorLoginTokenRepository,
    private val authUserRepository: AuthUserRepository,
    private val jwtService: JwtService,
    private val emailService: EmailService,
    @Value("\${professor-auth.token-expiry-minutes:15}")
    private val tokenExpiryMinutes: Int = 15,
    @Value("\${professor-auth.token-length:64}")
    private val tokenLength: Int = 64,
    @Value("\${professor-auth.rate-limit-requests:10}")
    private val rateLimitRequests: Int = 10,
    @Value("\${professor-auth.rate-limit-window-minutes:60}")
    private val rateLimitWindowMinutes: Int = 60,
    @Value("\${professor-auth.frontend-redirect-url:http://localhost:3000}")
    private val frontendRedirectUrl: String = "http://localhost:3000",
) {
    private val emailRateLimiters = ConcurrentHashMap<String, RateLimitEntry>()

    @Transactional
    fun requestLoginLink(email: String): RequestProfessorLoginLinkResponse {
        if (!email.isValidEmail()) {
            throw IllegalArgumentException("Invalid email address")
        }

        checkRateLimit(email)

        val professor = professorRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Professor not found")

        val token = generateSecureToken(tokenLength)
        val expiresAt = Instant.now().plusSeconds((tokenExpiryMinutes * 60).toLong())
        val loginToken = ProfessorLoginToken(
            professor = professor,
            token = token,
            expiresAt = expiresAt,
        )

        professorLoginTokenRepository.save(loginToken)

        val loginLink = "$frontendRedirectUrl/professor-login/verify?token=$token"
        emailService.sendProfessorLoginLink(professor, loginLink)

        return RequestProfessorLoginLinkResponse(
            message = "A magic login link has been sent to your email"
        )
    }

    @Transactional
    fun verifyLoginLink(token: String): VerifyProfessorLoginLinkResponse {
        val loginToken = professorLoginTokenRepository.findByToken(token)
            ?: throw IllegalArgumentException("Invalid token")

        if (loginToken.isExpired()) {
            throw IllegalArgumentException("Token expired")
        }

        if (loginToken.isAlreadyUsed()) {
            throw IllegalArgumentException("Token already used")
        }

        loginToken.markAsUsed()
        professorLoginTokenRepository.save(loginToken)

        val professor = loginToken.professor
        
        // Ensure AuthUser exists for this professor
        var authUser = authUserRepository.findByUsername(professor.email).orElse(null)
        if (authUser == null) {
            authUser = AuthUser(
                username = professor.email,
                password = "",
                role = UserRole.PROFESSOR,
                professor = professor
            )
            authUser = authUserRepository.save(authUser)
        }
        
        val principal = AuthUserPrincipal.from(authUser)
        val jwtToken = jwtService.generateToken(principal)

        return VerifyProfessorLoginLinkResponse(
            accessToken = jwtToken.token,
            redirectUrl = "/",
        )
    }

    private fun generateSecureToken(length: Int): String {
        val allowedChars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        return (1..length)
            .map { allowedChars[Random.nextInt(allowedChars.length)] }
            .joinToString("")
    }

    private fun checkRateLimit(email: String) {
        val now = System.currentTimeMillis()
        val windowMs = rateLimitWindowMinutes * 60 * 1000L
        
        val entry = emailRateLimiters[email]
        
        if (entry != null && now - entry.resetTime < windowMs) {
            if (entry.count >= rateLimitRequests) {
                throw IllegalArgumentException("Too many requests. Please try again later.")
            }
            emailRateLimiters[email] = RateLimitEntry(entry.count + 1, entry.resetTime)
        } else {
            emailRateLimiters[email] = RateLimitEntry(1, now)
        }
    }

    private fun String.isValidEmail(): Boolean {
        return this.isNotBlank() && this.contains("@") && this.contains(".")
    }
}
