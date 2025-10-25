package ar.edu.uns.cs.thesisflow.auth.dto

import java.time.Instant

data class LoginResponse(
    val token: String,
    val expiresAt: Instant,
    val role: String,
    val userId: String,
    val professorId: String?,
)
