package ar.edu.uns.cs.thesisflow.auth.api

import ar.edu.uns.cs.thesisflow.auth.dto.LoginRequest
import ar.edu.uns.cs.thesisflow.auth.dto.LoginResponse
import ar.edu.uns.cs.thesisflow.auth.model.AuthUserPrincipal
import ar.edu.uns.cs.thesisflow.auth.service.AuthService
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
}
