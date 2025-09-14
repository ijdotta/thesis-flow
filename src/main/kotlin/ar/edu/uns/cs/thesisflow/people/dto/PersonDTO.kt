package ar.edu.uns.cs.thesisflow.people.dto

import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.validation.CreatePerson
import jakarta.validation.constraints.NotBlank

data class PersonDTO(
    val publicId: String?,
    @NotBlank(message = "name is required", groups = [CreatePerson::class])
    val name: String?,
    @NotBlank(message = "lastname is required", groups = [CreatePerson::class])
    val lastname: String?,
) {
    fun toEntity() = Person(name = name!!, lastname =  lastname!!)
    fun update(person: Person) {
        name?.let { person.name = it }
        lastname?.let { person.lastname = it }
    }
}

fun Person.toDTO() = PersonDTO(publicId.toString(), name, lastname)