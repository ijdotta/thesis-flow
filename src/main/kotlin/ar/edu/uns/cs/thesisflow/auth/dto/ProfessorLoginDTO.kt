package ar.edu.uns.cs.thesisflow.auth.dto

data class RequestProfessorLoginLinkRequest(
    val email: String,
)

data class RequestProfessorLoginLinkResponse(
    val message: String,
)

data class VerifyProfessorLoginLinkRequest(
    val token: String,
)

data class VerifyProfessorLoginLinkResponse(
    val accessToken: String,
    val redirectUrl: String = "/",
)
