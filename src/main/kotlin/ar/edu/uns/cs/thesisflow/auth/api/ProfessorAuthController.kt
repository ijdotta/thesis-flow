package ar.edu.uns.cs.thesisflow.auth.api

import ar.edu.uns.cs.thesisflow.auth.dto.RequestProfessorLoginLinkRequest
import ar.edu.uns.cs.thesisflow.auth.dto.RequestProfessorLoginLinkResponse
import ar.edu.uns.cs.thesisflow.auth.dto.VerifyProfessorLoginLinkRequest
import ar.edu.uns.cs.thesisflow.auth.dto.VerifyProfessorLoginLinkResponse
import ar.edu.uns.cs.thesisflow.auth.service.ProfessorAuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth/professor")
class ProfessorAuthController(
    private val professorAuthService: ProfessorAuthService,
) {
    @PostMapping("/request-login-link")
    fun requestLoginLink(
        @RequestBody request: RequestProfessorLoginLinkRequest,
    ): ResponseEntity<Any> {
        return try {
            val response = professorAuthService.requestLoginLink(request.email)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            handleError(e.message ?: "Unknown error")
        }
    }

    @PostMapping("/verify-login-link")
    fun verifyLoginLink(
        @RequestBody request: VerifyProfessorLoginLinkRequest,
    ): ResponseEntity<Any> {
        return try {
            val response = professorAuthService.verifyLoginLink(request.token)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            handleError(e.message ?: "Unknown error")
        }
    }

    private fun handleError(message: String): ResponseEntity<Any> {
        val statusCode = when {
            message.contains("Invalid email") -> HttpStatus.BAD_REQUEST
            message.contains("Professor not found") -> HttpStatus.NOT_FOUND
            message.contains("Too many requests") -> HttpStatus.TOO_MANY_REQUESTS
            message.contains("Token") && message.contains("expired") -> HttpStatus.BAD_REQUEST
            message.contains("Token") && message.contains("used") -> HttpStatus.BAD_REQUEST
            message.contains("Invalid token") -> HttpStatus.NOT_FOUND
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }

        return ResponseEntity.status(statusCode).body(
            mapOf(
                "error" to message,
                "message" to message,
            )
        )
    }
}
