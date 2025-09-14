package ar.edu.uns.cs.thesisflow.people.dto

import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.validation.CreatePerson
import jakarta.validation.constraints.NotBlank

data class PersonDTO(
    val id: Long? = null,
    val publicId: String? = null,
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

fun Person.toDTO() = PersonDTO(
    id = id,
    publicId = publicId.toString(),
    name = name,
    lastname = lastname,
)