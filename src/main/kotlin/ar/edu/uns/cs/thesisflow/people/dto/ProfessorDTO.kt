package ar.edu.uns.cs.thesisflow.people.dto

import ar.edu.uns.cs.thesisflow.people.validation.CreateProfessor
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class ProfessorDTO(
    var id: Long? = null,
    var publicId: String? = null,
    var personPublicId: String? = null,
    var name: String? = null,
    var lastname: String? = null,
    @field:Email(message = "email must be valid", groups = [CreateProfessor::class])
    @field:NotBlank(message = "email is required", groups = [CreateProfessor::class])
    var email: String? = null,
)