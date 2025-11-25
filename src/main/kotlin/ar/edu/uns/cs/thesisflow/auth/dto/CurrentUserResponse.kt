package ar.edu.uns.cs.thesisflow.auth.dto

data class CurrentUserResponse(
    val id: String,
    val username: String,
    val role: String,
    val name: String,
    val email: String?,
)
