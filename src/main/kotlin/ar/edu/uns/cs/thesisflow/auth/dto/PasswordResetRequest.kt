package ar.edu.uns.cs.thesisflow.auth.dto

data class PasswordResetRequest(
    val currentPassword: String,
    val newPassword: String,
)

data class PasswordResetResponse(
    val success: Boolean,
    val message: String,
)
