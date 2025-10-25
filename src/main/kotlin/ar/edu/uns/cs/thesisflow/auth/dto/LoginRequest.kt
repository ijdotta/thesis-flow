package ar.edu.uns.cs.thesisflow.auth.dto

data class LoginRequest(
    val username: String,
    val password: String,
)
