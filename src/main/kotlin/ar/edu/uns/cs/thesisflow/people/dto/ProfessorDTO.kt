package ar.edu.uns.cs.thesisflow.people.dto

import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Professor
import ar.edu.uns.cs.thesisflow.people.validation.CreateProfessor
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class ProfessorDTO(
    val id: Long? = null,
    val publicId: String? = null,
    val personPublicId: String? = null,
    val name: String? = null,
    val lastname: String? = null,
    @field:Email(message = "email must be valid", groups = [CreateProfessor::class])
    @field:NotBlank(message = "email is required", groups = [CreateProfessor::class])
    val email: String? = null,
) {
    fun toEntity(person: Person) = Professor(person = person, email = email!!)
    fun update(professor: Professor) {
        email?.let { professor.email = it }
    }
}

fun Professor.toDTO() = ProfessorDTO(
    id = id,
    publicId = publicId.toString(),
    personPublicId = person.publicId.toString(),
    name = person.name,
    lastname = person.lastname,
    email = email
)