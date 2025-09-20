package ar.edu.uns.cs.thesisflow.people.dto

import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Professor

data class ProfessorDTO(
    val id: Long? = null,
    val publicId: String? = null,
    val personPublicId: String? = null,
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
    email = email
)