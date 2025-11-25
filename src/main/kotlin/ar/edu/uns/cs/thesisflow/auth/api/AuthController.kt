package ar.edu.uns.cs.thesisflow.auth.api

import ar.edu.uns.cs.thesisflow.auth.dto.LoginRequest
import ar.edu.uns.cs.thesisflow.auth.dto.LoginResponse
import ar.edu.uns.cs.thesisflow.auth.dto.PasswordResetRequest
import ar.edu.uns.cs.thesisflow.auth.dto.PasswordResetResponse
import ar.edu.uns.cs.thesisflow.auth.model.AuthUserPrincipal
import ar.edu.uns.cs.thesisflow.auth.service.AuthService
import ar.edu.uns.cs.thesisflow.auth.service.CurrentUserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val currentUserService: CurrentUserService,
) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        val authentication = authService.authenticate(request.username, request.password)
        val token = authService.generateToken(authentication)
        val principal = authentication.principal as AuthUserPrincipal

        val response = LoginResponse(
            token = token.token,
            expiresAt = token.expiresAt,
            role = principal.role.name,
            userId = principal.publicId.toString(),
            professorId = principal.professorPublicId?.toString(),
        )
        return ResponseEntity.ok(response)
    }

    @PostMapping("/reset-password")
    fun resetPassword(@RequestBody request: PasswordResetRequest): ResponseEntity<PasswordResetResponse> {
        try {
            val currentUser = currentUserService.requireCurrentUser()
            val userId = currentUser.getId() ?: throw IllegalStateException("User ID not found")
            authService.resetPassword(userId, request.currentPassword, request.newPassword)
            return ResponseEntity.ok(PasswordResetResponse(
                success = true,
                message = "Password reset successfully"
            ))
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().body(PasswordResetResponse(
                success = false,
                message = e.message ?: "Password reset failed"
            ))
        }
    }
}
